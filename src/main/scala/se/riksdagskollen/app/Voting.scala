package se.riksdagskollen.app

import java.util.Date

case class Voting(
  id: String,
  date: Date,
  databaseId: Option[BigInt] = None
  ) extends DatabaseModel {

  override def withDatabaseId(id: BigInt): DatabaseModel = copy(databaseId = Some(id))

}
