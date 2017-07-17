package server

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume}
import akka.actor.{Actor, ActorKilledException, ActorLogging, OneForOneStrategy, Props}
import com.typesafe.config.Config


/**
  * Created by brad on 2016-10-19.
  */

object Supervisor {

  def props(config : Config): Props =
    Props(new Supervisor(config))

}

class Supervisor (config : Config) extends Actor  with ActorLogging {

  log.info("*********************** supervisor start ***************************")

  val localip : String = config.getString("LocalIP")

  // Rule Engine
  val ruleEngine = context.actorOf(RuleEngine.props(), "RuleEngine")

  // TW actor -- WebService
  val relayWS = context.actorOf(TalkToWebService.props(), "TalkToWebService")

  // TM actor -- Management (MG)
  val relayMG = context.actorOf(TalkToMG.props(), "TalkToMG")

  // TG actor -- GateWay
  val portGW : Int = config.getInt("PortForGateWay")
  val relayGW = context.actorOf(TalkToGateway.props(portGW,localip), "TalkToGateway")

  // TD actor -- Data Analysis
  val portDA : Int = config.getInt("PortForDataAnalysis")
  val relayDA = context.actorOf(TalkToDataAnalysis.props(portDA,localip), "TalkToDataAnalysis")



  def receive = PartialFunction.empty
  override val supervisorStrategy =
    OneForOneStrategy() {
      case ake: ActorKilledException =>   log.info("[Supervisor]   ActorKilledException Restart") ; Restart
      case re: RuntimeException =>   log.info("[Supervisor] RuntimeException Restart") ; Restart
      case ne: NullPointerException =>   log.info("[Supervisor] NullPointerException Restart") ; Restart
      case _ =>  log.info("[Supervisor] _  Resume") ;  Resume
    }
}
