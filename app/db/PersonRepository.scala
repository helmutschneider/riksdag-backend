package db

import java.sql.{ResultSet, Connection}

/**
 * Created by Johan on 2015-09-12.
 */
class PersonRepository(db: Connection) {

  def getLatest(): Seq[Person] = {

    val sql =
      """
        |select *
        |from person p
        |where p.sync_id = (
        | select max(sync_id)
        | from sync
        | where completed_at is not null
        |)
      """.stripMargin

    val stmt = db.prepareStatement(sql)
    val result = stmt.executeQuery()

    val builder = List.newBuilder[Person]
    while ( result.next() ) {
      builder += PersonRepository.resultToPerson(result)
    }

    builder.result()
  }

}

object PersonRepository {

  def resultToPerson(r: ResultSet): Person = {
    new Person(
      r.getString("remote_id"),
      r.getInt("birth_year"),
      r.getInt("gender"),
      r.getString("first_name"),
      r.getString("last_name"),
      Option(r.getString("party")),
      Option(r.getString("location")),
      r.getInt("status"),
      r.getInt("sync_id")
    )
  }

}