package stats

import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Johan on 2015-08-26.
 */
case class GenderDistribution(female: Int, male: Int) {



}

object GenderDistribution {
  val jsonWriter = new Writes[GenderDistribution] {
    override def writes(o: GenderDistribution): JsValue = Json.obj(
      "female" -> o.female,
      "male" -> o.male
    )
  }
}
