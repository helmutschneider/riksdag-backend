package se.riksdagskollen.db

import java.sql.{Connection, ResultSet}

import se.riksdagskollen.app.Voting

class VotingRepository(db: Connection) extends Repository[Voting] {
  val builder = new QueryBuilder(db)

  def insert(data: Voting, syncId: Int): Boolean = {
    val stmt = builder.insert("voting", data.toMap + ("sync_id" -> syncId))
    val res = stmt.execute()
    stmt.close()
    res
  }

  override def resultSetToObject(result: ResultSet): Voting = {
    Voting(
      result.getString("voting_id"),
      result.getTimestamp("date"),
      result.getString("title")
    )
  }

  def latest(): Seq[Voting] = {
    val stmt = db.prepareStatement(
      s"""
         |select *
         |from voting t1
         |inner join (
         |   select max(sync_id) as sync_id
         |   from sync
         |   where completed_at is not null
         |) t2
         |on t1.sync_id = t2.sync_id
      """.stripMargin)
    val res = select(stmt)
    stmt.close()
    res
  }
}
