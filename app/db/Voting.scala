package db

import java.sql.Timestamp

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-25.
 */
class Voting(@Column("remote_id") val remoteId: String,
           val date: Timestamp,
           @Column("sync_id") val syncId: Int
            ) extends KeyedEntity[Int] {

  @Column("voting_id")
  var id: Int = 0

}
