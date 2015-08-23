package controllers

import db.Person
import http.{HttpClient}
import play.api.db.DB
import play.api.libs.ws.WS
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import org.squeryl.PrimitiveTypeMode._

class Application extends Controller {

  def get = Action {

    import db.Schema

    transaction {
      val people = from(db.Schema.people)(p => select(p)).toList
      val names = people.map(p => p.firstName)

      db.Schema.people.insert(new Person(0, "my-id", 1980, 0, "Helmut", "Schneider", "HSPARTI123", "WORLD", "some-url"))

      Ok(names.toString())
    }





    /*
    val client = new HttpClient()
    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withMethod("GET")
      .withQueryString(
        "utformat" -> "json"
      )

    implicit val reader = remote.Person.jsonReader

    client.send(request)
      .map(res => (res.json \ "personlista" \ "person").as[List[remote.Person]])
      .map(p => Ok(p.toString()))
      */

  }

}
