/**
  * Created by brad on 2016-10-14.
  */

package server


import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorDSL, ActorIdentity, ActorKilledException, ActorLogging, ActorRef, ActorSystem, DeathPactException, Identify, OneForOneStrategy, Props, SupervisorStrategy}
import akka.actor.ActorDSL.inbox
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.io.Tcp.Write
import akka.io.{IO, Tcp}
import akka.util.ByteString
import server.RuleEngine.{fromRE, fromTGH, fromTGHInit}


object TalkToGateway {

  def props(port: Int, localIP : String): Props =
    Props(new TalkToGateway(port,localIP))

}


class TalkToGateway(port: Int, localIP : String) extends Actor with ActorLogging {

  import Tcp._
  import context.system


  // 포트설정. 액터가 종료되면 자동적으로 포트는 해제된다.
  override def preStart(): Unit = {
    IO(Tcp) ! Bind(self, new InetSocketAddress(localIP, port))
  }

  def receive = {
    case Bound(localAddress) =>
      log.info("listening on port {}", localAddress.getPort)

    case CommandFailed(Bind(_, local, _, _, _)) =>
      log.warning(s"cannot bind to [$local]")
      context stop self

    case Connected(remote, local) =>
      log.info("received connection from {}", remote)
      val handler = context.actorOf(GatewayHandler.props(sender(), remote))
      sender() ! Register(handler, keepOpenOnPeerClosed = true)
    }

  // 자식액터들이 고장 났을 경우 전략
  override val supervisorStrategy =
    OneForOneStrategy() {
      case dp : DeathPactException => log.info("[TalkToGateway]   DeathPactException Resume") ; Resume
      case ake: ActorKilledException =>   log.info("[TalkToGateway]   ActorKilledException Restart") ; Restart
      case re: RuntimeException =>   log.info("[TalkToGateway]  RuntimeException Restart") ; Stop
      case _ =>  log.info("[TalkToGateway] exception  Resume") ;  Stop
    }
}

object GatewayHandler {
  final case class Ack(offset: Int) extends Tcp.Event

  def props(connection: ActorRef, remote: InetSocketAddress): Props =
    Props(classOf[GatewayHandler], connection, remote)
}


// ACK-Based Write Back-Pressure 및 Push-reading 을 사용한다. (http://hamait.tistory.com/664 참고)
class GatewayHandler(connection: ActorRef, remote: InetSocketAddress)
  extends Actor with ActorLogging {


  import Tcp._
  import GatewayHandler._


  private var storageOffset = 0
  private var storage = Vector.empty[ByteString]
  private var stored = 0L
  private var transferred = 0L

  val maxStored = 100000000L
  val highWatermark = maxStored * 5 / 10
  val lowWatermark = maxStored * 3 / 10
  private var suspended = false

  private def currentOffset = storageOffset + storage.size


  // 커넥션을 관찰함. PeerClosed 메세지 즉 커넥션이 멈추면 이 액터는 종료된다.
  context watch connection

  def receive = auth

  val ruleEngine  = context.actorSelection(ActorPaths.RuleEnginePath)

  var gwID : String = _

  def auth: Receive = {
    case Received(data) =>
      gwID  = data.utf8String
      log.info(s"accept gw : $gwID")
      ruleEngine ! fromTGHInit(gwID)
      context become writing
  }


  def writing: Receive = {

    case Received(data) =>
      log.info(s"fromGW -  '${data.utf8String}'")
      ruleEngine ! fromTGH(data.utf8String)

    case fromRE(data) =>   // from RE
      val dataWithNewLine = data + "\n"
      log.info(s"fromRE -  '$data'")
      connection ! Write(ByteString(dataWithNewLine), Ack(currentOffset))
      buffer(ByteString(dataWithNewLine))

    case data : ByteString =>
      connection ! Write(data, Ack(currentOffset))
      buffer(data)

    case Ack(ack) =>
      acknowledge(ack)

    case CommandFailed(Write(_, Ack(ack))) =>
      connection ! ResumeWriting
      context become buffering(ack)

    case PeerClosed =>
      if (storage.isEmpty) context stop self
      else context become closing
  }

  def buffering(nack: Int): Receive = {
    var toAck = 10
    var peerClosed = false

    {
      case data : ByteString       => buffer(data)
      case WritingResumed         => writeFirst()
      case PeerClosed             => peerClosed = true
      case Ack(ack) if ack < nack => acknowledge(ack)
      case Ack(ack) =>
        acknowledge(ack)
        if (storage.nonEmpty) {
          if (toAck > 0) {
            // stay in ACK-based mode for a while
            writeFirst()
            toAck -= 1
          } else {
            // then return to NACK-based again
            writeAll()
            context become (if (peerClosed) closing else writing)
          }
        } else if (peerClosed) context stop self
        else context become writing
    }
  }

  def closing: Receive = {
    case CommandFailed(_: Write) =>
      connection ! ResumeWriting
      context.become({

        case WritingResumed =>
          writeAll()
          context.unbecome()

        case ack: Int => acknowledge(ack)

      }, discardOld = false)

    case Ack(ack) =>
      acknowledge(ack)
      if (storage.isEmpty) context stop self
  }


  override def preStart(): Unit = {
    log.info("GatewayHandler preStart !!!")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("GatewayHandler preRestart !!!")
  }

  override def postStop(): Unit = {
    log.info("GatewayHandler postStop !!!")
    log.info(s"transferred $transferred bytes from/to [$remote]")
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("GatewayHandler postRestart !!!")
  }


  private def buffer(data: ByteString): Unit = {
    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      log.warning(s"drop connection to [$remote] (buffer overrun)")
      context stop self

    } else if (stored > highWatermark) {
      log.debug(s"suspending reading at $currentOffset")
      connection ! SuspendReading
      suspended = true
    }
  }

  private def acknowledge(ack: Int): Unit = {
    require(ack == storageOffset, s"received ack $ack at $storageOffset")
    require(storage.nonEmpty, s"storage was empty at ack $ack")

    val size = storage(0).size
    stored -= size
    transferred += size

    storageOffset += 1
    storage = storage drop 1

    if (suspended && stored < lowWatermark) {
      log.debug("resuming reading")
      connection ! ResumeReading
      suspended = false
    }
  }

  private def writeFirst(): Unit = {
    connection ! Write(storage(0), Ack(storageOffset))
  }

  private def writeAll(): Unit = {
    for ((data, i) <- storage.zipWithIndex) {
      connection ! Write(data, Ack(storageOffset + i))
    }
  }

}

