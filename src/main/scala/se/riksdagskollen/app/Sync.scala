package se.riksdagskollen.app

import java.sql.Timestamp

case class Sync(
  startedAt: Timestamp,
  completedAt: Timestamp,
  databaseId: Option[BigInt] = None
  ) extends DatabaseModel {

  override def withDatabaseId(id: BigInt): DatabaseModel = copy(databaseId = Some(id))

}
