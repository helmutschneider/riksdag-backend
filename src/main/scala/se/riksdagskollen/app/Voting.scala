package se.riksdagskollen.app

import java.util.Date

import se.riksdagskollen.db.Model

case class Voting(
  id: String,
  date: Date,
  title: String
  ) extends Model {

  override def toMap: Map[String, Any] = Map(
    "voting_id" -> id,
    "date" -> date,
    "title" -> title
  )

}
