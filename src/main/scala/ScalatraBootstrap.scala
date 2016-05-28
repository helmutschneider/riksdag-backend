import java.sql.{Connection, DriverManager}

import org.scalatra._
import javax.servlet.ServletContext
import java.io.File

import se.riksdagskollen.app.{AppController, ConfigParser}

class ScalatraBootstrap extends LifeCycle {

  var config: Map[String, String] = Map()
  private var dbConnection: Option[Connection] = None
  val connectionProvider: () => Connection = () => {
    dbConnection match {
      case Some(conn) if !conn.isClosed => conn
      case _ =>
        val props = new java.util.Properties()
        props.put("user", config("DB_USER"))
        props.put("password", config("DB_PASSWORD"))
        props.put("useSSL", "false")
        val conn = DriverManager.getConnection(config("DB_URL"), props)
        dbConnection = Some(conn)
        conn
    }
  }

  override def init(context: ServletContext) {
    super.init(context)

    val parser = new ConfigParser
    val path = System.getenv("ENV_PATH")
    val f = new File(if (path != null) path else ".env")
    config = parser.parse(f)

    context.mount(new AppController(connectionProvider), "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)

    dbConnection match {
      case Some(conn) => conn.close()
      case None =>
    }
  }
}
