import java.util.Calendar
import java.util.concurrent.TimeUnit

import http.HttpClient
import play.api.libs.concurrent.Akka
import play.api.{Logger, Application, GlobalSettings}
import remote.SyncManager

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-27.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    implicit val currentApp = app

    val currentDate = new java.util.Date()
    val runAtHour = 3

    val cal = Calendar.getInstance()
    println(cal.getTime)

    cal.set(Calendar.DATE, if (cal.get(Calendar.HOUR_OF_DAY) >= runAtHour) cal.get(Calendar.DATE) + 1 else cal.get(Calendar.DATE))
    cal.set(Calendar.HOUR_OF_DAY, runAtHour)
    println(cal.getTime.toString)

    val wait = cal.getTime.getTime - currentDate.getTime

    Logger.info(s"Waiting ${wait} ms before syncing next time")

    Akka.system.scheduler.schedule(
      Duration.create(wait, TimeUnit.MILLISECONDS),
      Duration.create(1, TimeUnit.DAYS)
    ) {
      val client = new HttpClient
      val mgr = new SyncManager(client)
      Logger.info("Starting sync")
      mgr.run().map(_ => Logger.info("Sync completed"))
    }
  }

}
