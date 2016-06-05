package se.riksdagskollen.app

case class Person(
  id: String,
  birthYear: Int,
  gender: Int,
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

object Person {
  object Gender {
    def parse(value: String): Int = {
      value.toLowerCase() match {
        case "kvinna" => 0
        case "man" => 1
        case _ => 2
      }
    }
  }
}
