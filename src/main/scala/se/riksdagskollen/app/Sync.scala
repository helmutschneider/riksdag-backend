package se.riksdagskollen.app

import java.sql.Timestamp

case class Sync(
  startedAt: Timestamp,
  completedAt: Option[Timestamp] = None,
  databaseId: Option[BigInt] = None
  ) extends DatabaseModel {

  override def withDatabaseId(id: BigInt): DatabaseModel = copy(databaseId = Some(id))
  def withCompletedAt(t: Timestamp) = copy(completedAt = Some(t))

}
