package se.riksdagskollen.db

import java.sql.Connection

import se.riksdagskollen.app.Vote

class VoteRepository(db: Connection) {

  val builder = new QueryBuilder(db)

  def insert(data: Vote, syncId: Int): Boolean = {
    val stmt = builder.insert("vote", data.toMap + ("sync_id" -> syncId))
    val res = stmt.execute()
    stmt.close()
    res
  }

  def insertAll(data: Seq[Vote], syncId: Int): Boolean = {
    val stmt = builder.insertAll("vote", data map { d => d.toMap + ("sync_id" -> syncId) })
    val res = stmt.execute()
    stmt.close()
    res
  }

  def mapToObject(data: Map[String, Any]): Vote = {
    Vote(
      data("value").asInstanceOf[Int],
      data("regarding").asInstanceOf[Int],
      data("voting_id").asInstanceOf[String],
      data("person_id").asInstanceOf[String]
    )
  }

}
