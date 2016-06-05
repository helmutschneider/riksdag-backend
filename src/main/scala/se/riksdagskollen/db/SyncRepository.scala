package se.riksdagskollen.db

import java.sql.{Connection, Timestamp}

import se.riksdagskollen.app.Sync

class SyncRepository(db: Connection) extends Repository[Sync] {
  val builder = new QueryBuilder(db)

  def insert(data: Sync): Int = {
    val stmt = builder.insert("sync", data.toMap)
    stmt.execute()
    val keys = stmt.getGeneratedKeys
    keys.next()
    val id = keys.getInt(1)
    keys.close()
    stmt.close()
    id
  }

  def update(data: Sync, id: Int): Boolean = {
    val stmt = builder.update("sync", data.toMap, Map("sync_id" -> id))
    val res = stmt.execute()
    stmt.close()
    res
  }

  override def mapToObject(data: Map[String, Any]): Sync = {
    Sync(
      data("started_at").asInstanceOf[Timestamp],
      Some(data("completed_at").asInstanceOf[Timestamp])
    )
  }
}
