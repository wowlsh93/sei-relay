
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.daemon.{Daemon, DaemonContext}
import server._

import scala.concurrent.duration._

//
//
//trait ApplicationLifecycle {
//  def start(): Unit
//  def stop(): Unit
//}
//
//
//abstract class AbstractApplicationDaemon extends Daemon {
//  def application: ApplicationLifecycle
//
//  def init(daemonContext: DaemonContext) {}
//
//  def start() = application.start()
//
//  def stop() = application.stop()
//
//  def destroy() = application.stop()
//}
//
//
//class ApplicationDaemon() extends AbstractApplicationDaemon {
//  def application = new SeiRelayApplication
//}
//
//
//object relaySystem extends App {
//
//  val application = new ApplicationDaemon
//
//  private[this] var cleanupAlreadyRun: Boolean = false
//
//  def cleanup(){
//    val previouslyRun = cleanupAlreadyRun
//    cleanupAlreadyRun = true
//    if (!previouslyRun) application.stop()
//  }
//
//  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
//    def run() {
//      cleanup()
//    }
//  }))
//
//  application.start()
//}
//
//
//class SeiRelayApplication() extends ApplicationLifecycle with LazyLogging  {
//
//  private[this] var started: Boolean = false
//
//  private val applicationName = "relayRemote"
//
//  val config =  ConfigFactory.load("application")
//  implicit val actorSystem =  ActorSystem(applicationName, config.getConfig("Provider"))
//
//  def start() {
//    logger.info(s"Starting $applicationName Service")
//
//    if (!started) {
//      started = true
//
//      val supervisor  = actorSystem.actorOf(Supervisor.props(config), "Supervisor")
//    }
//  }
//
//  def stop() {
//    logger.info(s"Stopping $applicationName Service")
//
//    if (started) {
//      started = false
//      actorSystem.shutdown()
//    }
//  }
//
//}


//object relaySystem extends App with LazyLogging{
//
//  logger.info("============= relaySystem start =============")
//  // relay system
//  val config =  ConfigFactory.load("application")
//
//  val system =  ActorSystem("relayRemote", config.getConfig("Provider"))
//
//  // supervisor have actors for supervising their life-cycle
//  val supervisor  = system.actorOf(Supervisor.props(config), "Supervisor")
//
//}

object relaySystem extends App with LazyLogging{
  logger.info("....................relaySystem start !!....................")
  // relay system
  val config =  ConfigFactory.load("application")

  val system =  ActorSystem("relayRemote", config.getConfig("Provider"))

  // supervisor have actors for supervising their life-cycle
  val supervisor  = system.actorOf(Supervisor.props(config), "Supervisor")

}
