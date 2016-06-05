import org.scalatra._
import javax.servlet.ServletContext
import java.io.File
import java.nio.file.{Path, Paths}

import com.mysql.jdbc.Driver
import org.apache.commons.dbcp2.{BasicDataSource, BasicDataSourceFactory}
import se.riksdagskollen.app.{AppController, Application, ConfigParser}
import se.riksdagskollen.http.{HttpClientTrait, ScalajHttpClient}

import scala.concurrent.ExecutionContext

class ScalatraBootstrap extends LifeCycle with Application {

  val root: Path = {
    var root = System.getenv("APP_ROOT")
    if (root == null) {
      root = "."
    }
    Paths.get(root)
  }

  val config: Map[String, String] = {
    val parser = new ConfigParser
    val path = root.resolve(".env").toFile
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
    props.put("connectionInitSqls", "set session sql_mode = 'STRICT_ALL_TABLES,ONLY_FULL_GROUP_BY,ANSI';")
    BasicDataSourceFactory.createDataSource(props)
  }

  val version: String = {
    val file = root.resolve("REVISION").toFile
    if (file.exists) {
      scala.io.Source.fromFile(file).mkString.trim
    }
    else {
      "dev-master"
    }
  }

  val httpClient: HttpClientTrait = new ScalajHttpClient(ExecutionContext.global)

  override def init(context: ServletContext) {
    super.init(context)
    context.mount(new AppController(this), "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    dataSource.close()
  }
}
