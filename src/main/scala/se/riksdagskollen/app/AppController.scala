package se.riksdagskollen.app

import java.sql.Connection
import scala.collection.mutable

class AppController(db: Connection) extends Servlet {

  case class Car(make: String, model: String)

  get("/") {
    val stmt = db.prepareCall("select * from car")
    val result = stmt.executeQuery()
    val items = mutable.Seq.newBuilder[Car]
    while (result.next()) {
      val make = result.getObject[String]("make", classOf[String])
      val model = result.getObject[String]("model", classOf[String])
      items += Car(make, model)
    }
    items
  }

}
