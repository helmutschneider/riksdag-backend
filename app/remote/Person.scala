package remote

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Created by Johan on 2015-08-15.
 */

object Gender extends Enumeration {
  type Gender = Value
  
  val Female = Value(0)
  val Male = Value(1)
  val Unknown = Value(2)

  def parse(gender: String): Gender = gender.toLowerCase match {
    case "man" | "male" => Male
    case "kvinna" | "female" => Female
    case _ => Unknown
  }

}

object Status extends Enumeration {
  type Status = Value

  val Active = Value(0)
  val Inactive = Value(100)

  def parse(status: String): Status = status.toLowerCase.replaceAll("[^\\w\\s]", "") match {
    case x if x.contains("tjnstgrande") => Status.Active
    case _ => Status.Inactive
  }

}

object Person {
  val jsonReader: Reads[Person] = (
      (JsPath \ "intressent_id").read[String] and
      (JsPath \ "fodd_ar").read[String].map(s => s.toInt) and
      (JsPath \ "kon").read[String].map(g => Gender.parse(g).id) and
      (JsPath \ "tilltalsnamn").read[String] and
      (JsPath \ "efternamn").read[String] and
      (JsPath \ "parti").readNullable[String] and
      (JsPath \ "valkrets").readNullable[String] and
      (JsPath \ "status").read[String].map (s => Status.parse(s).id)
    )(Person.apply _)

}


case class Person(remoteId: String,
                  birthYear: Int,
                  gender: Int,
                  firstName: String,
                  lastName: String,
                  party: Option[String],
                  location: Option[String],
                  status: Int) {

  def toDbPerson(syncId: Int): db.Person = {
    new db.Person(
      this.remoteId,
      this.birthYear,
      this.gender,
      this.firstName,
      this.lastName,
      this.party,
      this.location,
      this.status,
      syncId)
  }

}
