package se.riksdagskollen.app

import se.riksdagskollen.db.Model

case class Person(
  id: String,
  birthYear: Int,
  gender: String,
  firstName: String,
  lastName: String,
  status: String,
  party: String
  ) extends Model {

  override def toMap = Map(
    "person_id" -> id,
    "birth_year" -> birthYear,
    "gender" -> gender,
    "first_name" -> firstName,
    "last_name" -> lastName,
    "status" -> status,
    "party" -> party
  )

}
