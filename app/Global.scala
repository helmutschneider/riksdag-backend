import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.Cancellable
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

  var syncTask: Option[Cancellable] = None

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    val currentDate = new java.util.Date()
    val runAtHour = 3

    val cal = Calendar.getInstance()
    cal.set(Calendar.DATE, if (cal.get(Calendar.HOUR_OF_DAY) >= runAtHour) cal.get(Calendar.DATE) + 1 else cal.get(Calendar.DATE))
    cal.set(Calendar.HOUR_OF_DAY, runAtHour)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)

    val wait = cal.getTime.getTime - currentDate.getTime

    Logger.info(s"Scheduling next sync for ${cal.getTime}")

    implicit val currentApp = app

    val task = Akka.system.scheduler.schedule(
      Duration.create(wait, TimeUnit.MILLISECONDS),
      Duration.create(1, TimeUnit.DAYS)
    ) {
      Logger.info("Starting sync")
      val client = new HttpClient
      val mgr = new SyncManager(client)
      mgr.run().map(_ => Logger.info("Sync completed"))
    }

    this.syncTask = Some(task)
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)

    syncTask match {
      case Some(x) => x.cancel()
      case _ =>
    }

  }

}
