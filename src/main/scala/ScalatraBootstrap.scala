import java.sql.{Connection, DriverManager}

import org.scalatra._
import javax.servlet.ServletContext

import se.riksdagskollen.app.AppController

class ScalatraBootstrap extends LifeCycle {

  var dbConnection: Option[Connection] = None

  override def init(context: ServletContext) {
    super.init(context)

    val props = new java.util.Properties()
    props.put("user", "root")
    props.put("password", "")
    props.put("useSSL", "false")
    dbConnection = Some(DriverManager.getConnection("jdbc:mysql://127.0.0.1/riks", props))

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
