package remote

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Created by Johan on 2015-08-15.
 */

object Gender extends Enumeration {
  type Gender = Value
  val Male, Female, Unknown = Value

  def parse(gender: String): Gender = gender.toLowerCase match {
    case "man" | "male" => Male
    case "kvinna" | "female" => Female
    case _ => Unknown
  }

}

object Person {
  val jsonReader: Reads[Person] = (
      (JsPath \ "intressent_id").read[String] and
      (JsPath \ "fodd_ar").read[String].map(s => s.toInt) and
      (JsPath \ "kon").read[String].map(g => Gender.parse(g)) and
      (JsPath \ "tilltalsnamn").read[String] and
      (JsPath \ "efternamn").read[String] and
      (JsPath \ "parti").read[String] and
      (JsPath \ "valkrets").read[String] and
      (JsPath \ "bild_url_max").read[String]
    )(Person.apply _)
}


case class Person(personId: String, birthYear: Int, gender: Gender,
                  firstName: String, lastName: String, party: String,
                  location: String, imageUrl: String) {

}

import sorm._

object  Db extends Instance(
  entities = Set() +Entity[Person](),
  url = "jdbc:mysql://theownagecool.se/riksdag_2",
  user = "romson",
  password = "vAd3XsVfc3Am37WW577N",
  initMode = InitMode.Create,
  poolSize = 1
)
