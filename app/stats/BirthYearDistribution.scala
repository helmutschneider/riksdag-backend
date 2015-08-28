package stats

import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Johan on 2015-08-28.
 */
case class BirthYearDistribution(birthYear: Int, count: Int) {

}

object BirthYearDistribution {

  val jsonWriter = new Writes[BirthYearDistribution] {
    override def writes(o: BirthYearDistribution): JsValue = Json.obj(
      "birth_year" -> o.birthYear,
      "count" -> o.count
    )
  }

}