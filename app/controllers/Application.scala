package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

class Application extends Controller {

  case class Thing(name: String)

  implicit val thingWrites = new Writes[Thing] {
    override def writes(o: Thing) = Json.obj(
      "name" -> o.name
    )
  }

  def get = Action {

    val a = Json.toJson(Seq(
      Thing("Car"),
      Thing("Boat")
    ))

    Ok(a.toString())
  }

}
