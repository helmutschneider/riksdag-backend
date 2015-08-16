
import play.api.{Application, GlobalSettings}
import play.api.Play.current
import app.Environment

/**
 * Created by Johan on 2015-08-16.
 */

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val file = play.api.Play.application.getFile(".env")

    Environment.parseEnvFile(file)
  }

}
