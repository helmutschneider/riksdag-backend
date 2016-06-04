package se.riksdagskollen.app

import java.sql.Timestamp

import se.riksdagskollen.db.Model

case class Sync(
  startedAt: Timestamp,
  completedAt: Option[Timestamp] = None
  ) extends Model {

  def withCompletedAt(t: Timestamp) = copy(completedAt = Some(t))

  override def toMap = Map(
    "started_at" -> startedAt,
    "completed_at" -> completedAt.orNull
  )
}
