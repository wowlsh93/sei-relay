package server

/**
  * Created by brad on 2016-10-19.
  */
object ActorPaths {

  val RuleEnginePath = "/user/Supervisor/RuleEngine"
  val TDPath = "/user/Supervisor/TalkToDataAnalysis/DataAnalysisHandler"
  val TMGPath = "/user/Supervisor/TalkToMG"
  val TWPath = "/user/Supervisor/TalkToWebService"
  val TMPath = "/user/Supervisor/TalkToMobileService"

}


object MsgDirection {

  val MD_TD = "Direction_TD"
  val MD_TMG = "Direction_TMG"
  val MD_TW = "Direction_TW"
  val MD_TM = "Direction_TM"

}
