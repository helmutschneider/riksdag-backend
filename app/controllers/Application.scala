package controllers

import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def get = Action.async {

    val conf = DatabaseConfigProvider.get[JdbcProfile](play.api.Play.current)
    val DB = conf.db

    import conf.driver.api._

    val q = db.Person.tableQuery.map(p => p)
    val action = q.result
    val result = DB.run(action)

    result.map(p => Ok(p.toString()))
  }

}
