package db

import slick.driver.MySQLDriver.api._
import java.sql.{Timestamp}

/**
 * Created by Johan on 2015-08-18.
 */
class SyncTable(tag: Tag) extends Table[(Int, Timestamp, Timestamp)](tag, "sync") {

  def id = column[Int]("sync_id", O.PrimaryKey, O.AutoInc)
  def startedAt = column[Timestamp]("started_at")
  def completedAt = column[Timestamp]("completed_at")

  def * = (id, startedAt, completedAt)

}

object SyncTable {
  val query = TableQuery[SyncTable]
}
