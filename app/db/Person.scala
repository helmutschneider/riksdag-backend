package db

import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Johan on 2015-08-24.
 */
class Person(@Column("remote_id") val remoteId: String,
             @Column("birth_year") val birthYear: Int,
             val gender: Int,
             @Column("first_name") val firstName: String,
             @Column("last_name") val lastName: String,
             val party: Option[String],
             val location: Option[String],
             val status: Int,
             @Column("sync_id") val syncId: Int
              ) extends KeyedEntity[Int] {

  @Column("person_id")
  var id: Int = 0

}

object Person {

  val jsonWriter = new Writes[Person] {
    override def writes(o: Person): JsValue = Json.obj(
      "remote_id" -> o.remoteId,
      "birth_year" -> o.birthYear,
      "gender" -> o.gender,
      "first_name" -> o.firstName,
      "last_name" -> o.lastName,
      "party" -> o.party,
      "location" -> o.location,
      "status" -> o.status
    )
  }

}