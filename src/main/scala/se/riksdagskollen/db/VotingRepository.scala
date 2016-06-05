package se.riksdagskollen.db

import java.sql.{Connection, ResultSet, Timestamp}

import se.riksdagskollen.app.Voting

class VotingRepository(db: Connection) extends Repository[Voting] {
  val builder = new QueryBuilder(db)
  val wrapped = new WrappedConnection(db)

  def insert(data: Voting, syncId: Int): Boolean = {
    val stmt = builder.insert("voting", data.toMap + ("sync_id" -> syncId))
    val res = stmt.execute()
    stmt.close()
    res
  }

  override def mapToObject(data: Map[String, Any]): Voting = {
    Voting(
      data("voting_id").asInstanceOf[String],
      data("date").asInstanceOf[Timestamp],
      data("title").asInstanceOf[String]
    )
  }

  def latest(): Seq[Voting] = {
    val sql =
      """
         |select *
         |from voting t1
         |inner join (
         |   select max(sync_id) as sync_id
         |   from sync
         |   where completed_at is not null
         |) t2
         |on t1.sync_id = t2.sync_id
      """.stripMargin

    wrapped.queryAll(sql) map { mapToObject }
  }
}
