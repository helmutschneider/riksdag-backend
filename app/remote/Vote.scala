/**
 * Created by Jacob on 2015-08-16.
 */
package remote

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Date


object Result extends Enumeration {
  type Result = Value
  val Yes, No, Absent = Value

  def parse(result: String): Result = result.toLowerCase match {
    case "ja" | "yes" => Yes
    case "nej" | "no" => No
    case _ => Absent
  }

}

object Vote {
  val jsonReader: Reads[Vote] = (
   (JsPath \ "votering_id").read[String] and
   (JsPath \ "systemdatum").read[Date]
  )(Vote.apply _)
}

object VoteCast {
  val jsonReader: Reads[VoteCast] = (
    (JsPath \ "votering_id").read[String] and
    (JsPath \ "intressent_id").read[String].map(s => s.toInt) and
    (JsPath \ "rost").read[String].map(g => Result.parse(g).id)
  )(VoteCast.apply _)
}


case class Vote(voteId: String, date: Date)
case class VoteCast(voteId: String, personId: Int, result: Int)
