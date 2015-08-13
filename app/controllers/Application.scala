package controllers

import _root_.http.HttpClient
import play.api._
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Application extends Controller {

  case class Thing(name: String)

  implicit val thingWrites = new Writes[Thing] {
    override def writes(o: Thing) = Json.obj(
      "name" -> o.name
    )
  }

  def get = Action.async {

    val client = new HttpClient

    val req = WS.url("http://data.riksdagen.se/voteringlista/")
      .withQueryString(
        "sz" -> 10.toString(),
        "rm" -> "2014/15",
        "utformat" -> "json"
      )
      .withMethod("GET")
      .withHeaders(
        "Content-Type" -> "application/json"
      )

    client.send(req).map(res => {
      println(res.status)


      Ok(res.body)
    })
  }

}
