package controllers

import _root_.http.HttpClient
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import remote._
import db.Db

class Application extends Controller {

  def get = Action.async {

    val client = new HttpClient
    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withQueryString(
        "utformat" -> "json"
      ).withMethod("GET")

    val voteRequest = WS.url("http://data.riksdagen.se/voteringlista/")
      .withQueryString(
        "rm" -> "2014/15",
        "sz" -> "500",
        "utformat" -> "json"
      ).withMethod("GET")


    client.send(voteRequest).map(res => {
      val json = res.json
      implicit val voteReader = Vote.jsonReader
      implicit val voteCastReader = VoteCast.jsonReader
      val voteList = (json \ "voteringlista" \ "votering").as[List[Vote]].distinct
      val voteCastList = (json \ "voteringlista" \ "votering").as[List[VoteCast]]

      println(voteCastList)
      Ok(voteCastList.mkString("\n\n"))
    })

    client.send(request).map(res => {
      val json = res.json
      implicit val reader = Person.jsonReader
      val arr = (json \ "personlista" \ "person").as[List[Person]]

//      Db.transaction({
//        for (person <- arr) {
//          Db.save(person)
//        }
//      })
//
      Ok(arr.mkString("\n\n"))
    })

  }

}
