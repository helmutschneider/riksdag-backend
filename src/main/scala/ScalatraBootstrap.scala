import java.sql.{Connection, DriverManager}

import org.scalatra._
import javax.servlet.ServletContext
import java.io.File

import se.riksdagskollen.app.{AppController, ConfigParser}

class ScalatraBootstrap extends LifeCycle {

  var config: Map[String, String] = Map()
  var dbConnection: Option[Connection] = None

  override def init(context: ServletContext) {
    super.init(context)

    val parser = new ConfigParser
    val path = System.getenv("ENV_PATH")
    val f = new File(if (path != null) path else ".env")
    config = parser.parse(f)

    val props = new java.util.Properties()
    props.put("user", config("DB_USER"))
    props.put("password", config("DB_PASSWORD"))
    props.put("useSSL", "false")
    dbConnection = Some(DriverManager.getConnection(config("DB_URL"), props))

    context.mount(new AppController(dbConnection.get), "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)

    dbConnection match {
      case Some(conn) => conn.close()
      case None =>
    }
  }
}
