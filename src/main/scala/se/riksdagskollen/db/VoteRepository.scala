package se.riksdagskollen.db

import java.sql.{Connection, ResultSet}

import se.riksdagskollen.app.Vote

class VoteRepository(db: Connection) extends Repository[Vote] {

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

  override def resultSetToObject(result: ResultSet): Vote = {
    Vote(
      result.getInt("value"),
      result.getInt("regarding"),
      result.getString("voting_id"),
      result.getString("person_id")
    )
  }

}
