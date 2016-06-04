package se.riksdagskollen.app

import se.riksdagskollen.db.Model

case class Vote(
  value: String,
  regarding: String,
  votingId: String,
  personId: String) extends Model {

  override def toMap = Map(
    "value" -> value,
    "regarding" -> regarding,
    "voting_id" -> votingId,
    "person_id" -> personId
  )

}
