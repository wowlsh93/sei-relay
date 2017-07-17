
/**
  * Created by brad on 2016-10-26.
  */


package server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.{CommandFailed, PeerClosed, Received, Write}
import akka.util.ByteString
import server.DataAnalysisHandler.Ack
import server.RuleEngine.{fromRE, fromTD, fromTDAInit, fromTMG}



object TalkToMG {

  def props(): Props =
    Props(new TalkToMG)

}

class TalkToMG  extends Actor {

  val log = Logging(context.system, this)

  def ruleEngine  = context.actorSelection(ActorPaths.RuleEnginePath)

  var mg : ActorRef = _

  def receive = {
    case "mg-1"  =>
      log.info(s"fromTMG :  mg-1 ")
      mg = sender()

    case msg : String =>
      log.info(s"fromTMG -  '$msg'")
      ruleEngine ! fromTMG(msg)
    case fromRE(data) =>   // from RE
      log.info(s"fromRE -  '$data'")
      mg ! data

    case _ =>
      log.info("received undefined msg erorr!!")
  }


  override def postStop() = log.info("TalkToMG going down")
}


