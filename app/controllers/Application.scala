package controllers

import _root_.http.HttpClient
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import remote.Person
import db.Db

class Application extends Controller {

  def get = Action.async {

    val client = new HttpClient
    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withQueryString(
        "utformat" -> "json"
      ).withMethod("GET")

    client.send(request).map(res => {
      val json = res.json

      implicit val reader = Person.jsonReader
      val arr = (json \ "personlista" \ "person").as[List[Person]]

      Db.transaction({
        for (person <- arr) {
          Db.save(person)
        }
      })

      Ok(arr.mkString("\n\n"))
    })

  }

}
