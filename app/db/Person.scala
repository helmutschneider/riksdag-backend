package db

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-24.
 */
class Person(@Column("remote_id") val remoteId: String,
             @Column("birth_year") val birthYear: Int,
             val gender: Int,
             @Column("first_name") val firstName: String,
             @Column("last_name") val lastName: String,
             val party: Option[String],
             val location: Option[String],
             val status: Int,
             @Column("sync_id") val syncId: Int
              ) extends KeyedEntity[Int] {

  @Column("person_id")
  var id: Int = 0

}
