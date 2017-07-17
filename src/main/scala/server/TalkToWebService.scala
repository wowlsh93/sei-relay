package server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import server.RuleEngine.{fromRE, fromTMG, fromTW}

/**
  * Created by brad on 2016-10-14.
  */

object Sender {
  case object Shutdown
}


object TalkToWebService {

  def props(): Props =
    Props(new TalkToWebService)

}

class TalkToWebService  extends Actor {
  import Sender._
  val log = Logging(context.system, this)

  def ruleEngine  = context.actorSelection(ActorPaths.RuleEnginePath)

  var ws : ActorRef = _


  def receive = {
    case "ws-1"  =>
      log.info(s"from : ws-1 ")
      ws = sender()

    case msg : String =>
      log.info(s"from ws -  '$msg'")
      ruleEngine ! fromTW(msg)

    case fromRE(data) =>   // from RE
      log.info(s"fromRE -  '$data'")
      ws ! data

    case Shutdown =>
      log.info("..................receive a Shudown message!...............")
      context.system.terminate()

    case _ =>
      log.info("received undefined msg erorr!!")
  }
  override def postStop() = log.info("TalkToWebService going down")
}


