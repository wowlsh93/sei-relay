package server

/**
  * Created by brad on 2016-10-19.
  */
object PKConst {

  val call_id = "CALL-ID"

  val gateway_id = "GATEWAY-ID"

  val switch_id = "SWITCH-ID"

  val cmd = "CMD"

  val  group_type = "GROUP-TYPE";

  val  state_type = "STATE-TYPE";

  val  direction_type = "DIRECTION-TYPE";

  val  content = "CONTENT"

  val  description = "DESCRIPTION"

  val  time = "TIME"

  // CMD type  ( device <--> server )

  val  card_auth = "CARD_AUTH";

  val  data_auth = "DATA_AUTH";


  // CMD type  ( device --> server )


  val  switch_on = "SWITCH-ON";

  val  switch_off = "SWITCH-OFF";


  // CMD type  ( server --> device )

  val  switch_get_on = "SWITCH-GET-ON";

  val  switch_get_attach = "SWITCH-GET-ATTACH";

  val  power_get = "POWER-GET";

  val  meter_electricity_get = "METER-ELE-GET";

  val  set_report_timer = "SET-REPORT-TIMER";

  val ip_change = "IP-CHANGE";

  val pattern_option_change = "PATTERN-OPTION-CHANGE"


  //  Report type ( device -> server) --------------------------------------------------------------------------------------//

  val meter_electricity_state = "METER-ELE-STATE"
  val power_state = "POWER-STATE";
  val unauth_detected = "UNAUTH-DETECTED"
  val ev_charge_started = "EV-CHARGE-START"
  val ev_charge_completed = "EV-CHARGE-COMPLETE"
  val master_start  = "MASTER-START"
  val master_end    = "MASTER-END"
  val overload_alert = "OVERLOAD-ALERT"
  // RESULT
  val success =  "SUCCESS"
  val fail    = "FAIL"


  // DIRECTION   TYPE

  val request = "REQUEST";    // 사용자의 요청
  val report = "REPORT";      // 기기에서 서버로
  val response = "RESPONSE";  // 기기의 요청에 따른 응답

  //  STATE  TYPE ( controller -> user)

  val alert = "ALERT";
  val recover = "RECOVER";
  val normal = "NORMAL";
  val emergency = "EMERGENCY";

  // GROUP   TYPE ( user -> controller )

  val group = "GROUP";
  val single = "SINGLE";

}
