package db

import java.sql.Timestamp

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-24.
 */

class Sync(@Column("started_at") val startedAt: Timestamp,
           @Column("completed_at") var completedAt: Option[Timestamp]
            ) extends KeyedEntity[Int] {

  @Column("sync_id")
  var id: Int = 0

}