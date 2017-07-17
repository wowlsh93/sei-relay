package server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import server.RuleEngine.fromTGH

import scala.util.parsing.json._
import scala.collection._

/**
  * Created by brad on 2016-10-18.
  */


object RuleEngine {

  def props(): Props =
    Props(new RuleEngine)

  case class fromTGHInit(gw_id : String)  // 개별 gateway handler 등록
  case class fromTDAInit(da_name : String)  // data analysis handler 등록

  case class fromTGH(msg: String)  // gateway 로 부터의 메세지
  case class fromTD(msg: String)  // data analysis 로 부터의 메시지
  case class fromTMG(msg: String) // manager 로 부터의 메세지
  case class fromTW(msg: String)  // web 으로 부터의 메세지
  case class fromTM(msg: String)  // mobile 로 부터의 메세지
  case class fromRE(msg: String)  // Rule Engine 로 부터의 메세지


}

class RuleEngine  extends Actor {
  import server.RuleEngine._

  val log = Logging(context.system, this)


  // 액터참조 :  핸들러는 Ref ,나머지는 Path 로 관리 :   http://hamait.tistory.com/680 참고
  val gwHandlers = mutable.Map[String, ActorRef]()
  val daHandler =  mutable.Map[String, ActorRef]()
  val analysis  = context.actorSelection(ActorPaths.TDPath)
  val manager  = context.actorSelection(ActorPaths.TMGPath)
  val web  = context.actorSelection(ActorPaths.TWPath)
  val mobile  = context.actorSelection(ActorPaths.TMPath)

  def receive = {

    case fromTDAInit(da_name) =>   // data analysis handler 등록
      log.info(s"fromTD : '$da_name'")
      daHandler("data_analaysis") = sender()

    case fromTGHInit(gw_id) =>     // 개별 gateway handler 등록
      log.info(s"fromTG : '$gw_id'")
      insertGWHanderRef(sender(), gw_id)

    case fromTGH(msgJson)   =>  // gateway로 부터 메세지
      log.info(s"fromTGH :  '$msgJson'")

      parsingMsgFromTGH(msgJson) match {
        case MsgDirection.MD_TD => log.info("Direction_TD") ;  daHandler.get("data_analaysis").get ! fromRE(msgJson)
        case MsgDirection.MD_TMG => log.info("Direction_TMG") ;  manager ! fromRE(msgJson)
        case MsgDirection.MD_TW => log.info("Direction_TW")
        case _=> log.info("No direction msg!!")
      }

    case fromTD(msgJson)   =>   // data analysis 로 부터 메세지
      log.info(s"fromTD :  '$msgJson'")
      sendGWHandler(msgJson)

    case fromTMG(msgJson)   =>  // manager 서비스로 부터 메세지
      log.info(s"fromTMG :  '$msgJson'")
      sendGWHandler(msgJson)

    case fromTW(msgJson)   =>  // 관리웹/앱 서비스로 부터 메세지
      log.info(s"fromTW :  '$msgJson'")
      sendGWHandler(msgJson)

    case _   =>
      log.info("something wrong - RuleEngine received no defined type data")

  }

  // 매니저 서비스로 부터의 메세지를 처리 한 후에 해당 게이트웨이로 전송
  // 1. 인증확인
  // 2. 업데이트 요청
  // 3. 데이터 패턴 분석 결과
  def sendGWHandler(msgJson : String) ={

    val result = JSON.parseFull(msgJson)

    val gw_id : Option[Any] = result match {
      case Some(map: Map[String,Any]) => map.get(PKConst.gateway_id)
      case None => println("Failed.") ; None
      case _ => println("Failed.") ; None
    }

    gw_id match {
      case Some("ALL") =>
          gwHandlers.foreach {case (k,v) => v ! fromRE(msgJson)}

      case Some(id : String) =>
        if (gwHandlers.get(id).isDefined)
          gwHandlers.get(id).get ! fromRE(msgJson)
        else
          println("Failed.") ;

      case None => "nothing"
    }
  }

  // 게이트웨이로 부터의 메세지를 분석해서 개별 서비스로 전송
  // 1. 인증확인
  // 2. 미터량
  // 3. 비인증 기기 알림
  // 4. 과부하 알림
  def parsingMsgFromTGH(msgJson : String): String ={

    val result = JSON.parseFull(msgJson)

    val cmd : Option[Any] = result match {
      case Some(map: Map[String,Any]) => map.get(PKConst.cmd)
      case None => println("Failed.") ; None
      case _ => println("Failed.") ; None
    }

    cmd match {
      case Some(msg: String) => msg match {
        case PKConst.data_auth => MsgDirection.MD_TD
        case PKConst.card_auth => MsgDirection.MD_TMG
        case PKConst.ev_charge_started => MsgDirection.MD_TMG
        case PKConst.ev_charge_completed => MsgDirection.MD_TMG
        case PKConst.meter_electricity_state => MsgDirection.MD_TMG
        case PKConst.unauth_detected  => MsgDirection.MD_TMG
        case PKConst.overload_alert => MsgDirection.MD_TMG

        case _ => "nothing"
      }
      case None => "nothing"
    }

  }

  def insertGWHanderRef(ref : ActorRef , gw_id : String): Unit = {
    gwHandlers(gw_id) = sender()
  }


  override def preStart(): Unit = {
    log.info("RuleEngine preStart !!!")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("RuleEngine preRestart !!!")
  }

  override def postStop(): Unit = {
    log.info("RuleEngine postStop !!!")
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("RuleEngine postRestart !!!")
  }
}