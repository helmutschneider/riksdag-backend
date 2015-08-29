package db

import java.sql.Timestamp

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-24.
 */
class Document(@Column("remote_id") val remoteId: String,
               @Column("published_at") val publishedAt: Timestamp,
               val title: String,
               @Column("voting_id") val votingId: Int
                ) extends KeyedEntity[Int] {

  @Column("document_id")
  var id: Int = 0

}
