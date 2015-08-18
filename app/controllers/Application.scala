package controllers

import db.{PersonTable, PersonRepository}
import http.{HttpClient}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WS
import play.api.mvc._
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

class Application extends Controller {

  def get = Action.async {

    val conf = DatabaseConfigProvider.get[JdbcProfile](play.api.Play.current)
    val DB = conf.db

    val repo = new PersonRepository(DB, PersonTable.query)

    val client = new HttpClient()
    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withMethod("GET")
      .withQueryString(
        "utformat" -> "json"
      )

    implicit val reader = remote.Person.jsonReader

    client.send(request).map(res => {
      (res.json \ "personlista" \ "person").as[List[remote.Person]]
    }).map(people => repo.save(people)).map(p => Ok(p.toString))

  }

}
