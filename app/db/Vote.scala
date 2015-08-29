package db

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-25.
 */
class Vote(@Column("person_id") val personId: Int,
           @Column("voting_id") val votingId: Int,
            val result: Int,
            val concerns: String
              ) extends KeyedEntity[Int] {

  @Column("vote_id")
  var id: Int = 0

}
