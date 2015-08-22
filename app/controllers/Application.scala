package controllers

import http.{HttpClient}
import play.api.libs.ws.WS
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

class Application extends Controller {

  def get = Action.async {

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

  }

}
