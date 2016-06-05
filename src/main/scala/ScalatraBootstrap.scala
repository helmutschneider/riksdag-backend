import org.scalatra._
import javax.servlet.ServletContext
import java.io.File
import java.nio.file.Paths

import com.mysql.jdbc.Driver
import org.apache.commons.dbcp2.{BasicDataSource, BasicDataSourceFactory}
import se.riksdagskollen.app.{AppController, ConfigParser}

class ScalatraBootstrap extends LifeCycle {

  val config: Map[String, String] = {
    val parser = new ConfigParser
    var root = System.getenv("APP_ROOT")
    if (root == null) {
      root = "."
    }
    val path = Paths.get(root, ".env")
    val f = new File(path.toString)
    parser.parse(f)
  }

  val dataSource: BasicDataSource = {
    val props = new java.util.Properties()
    props.put("url", config("DB_URL"))
    props.put("username", config("DB_USER"))
    props.put("password", config("DB_PASSWORD"))
    props.put("driverClassName", classOf[Driver].getCanonicalName)
    props.put("connectionProperties", "useSSL=false;")
    BasicDataSourceFactory.createDataSource(props)
  }

  override def init(context: ServletContext) {
    super.init(context)
    context.mount(new AppController(dataSource), "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    dataSource.close()
  }
}
