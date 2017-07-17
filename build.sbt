
name := "sei-relay"

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.11"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.11"

libraryDependencies += "commons-daemon" % "commons-daemon" % "1.0.15"


// akka 에 외부로깅모듈 추가
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.8")


// scala 방식을 추가함
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"

enablePlugins(JavaAppPackaging)

scriptClasspath +="../conf"