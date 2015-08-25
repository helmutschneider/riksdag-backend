package remote

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Date


object Result extends Enumeration {
  type Result = Value
  val No, Yes, Absent, Abstaining = Value

  def parse(result: String): Result = result.toLowerCase.replaceAll("[^\\w]", "") match {
    case "ja" => Yes
    case "nej" => No
    case "avstr" => Abstaining
    case "frnvarande" => Absent
  }

}

object Voting {
  val jsonReader: Reads[Voting] = (
   (JsPath \ "votering_id").read[String] and
   (JsPath \ "systemdatum").read[Date]
  )(Voting.apply _)
}

object Vote {
  val jsonReader: Reads[Vote] = (
    (JsPath \ "votering_id").read[String] and
    (JsPath \ "intressent_id").read[String] and
    (JsPath \ "rost").read[String].map(g => Result.parse(g).id)
  )(Vote.apply _)
}


case class Voting(remoteId: String, date: Date)
case class Vote(remoteId: String, remotePersonId: String, result: Int)
