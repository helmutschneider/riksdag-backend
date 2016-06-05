package se.riksdagskollen.db

import java.sql.{Connection, ResultSet}

import se.riksdagskollen.app.Person

class PersonRepository(db: Connection) extends Repository[Person] {
  val builder = new QueryBuilder(db)

  def insert(data: Person, syncId: Int): Boolean = {
    val stmt = builder.insert("person", data.toMap + ("sync_id" -> syncId))
    val res = stmt.execute()
    stmt.close()
    res
  }

  def insertAll(data: Seq[Person], syncId: Int): Boolean = {
    val stmt = builder.insertAll("person", data map { d => d.toMap + ("sync_id" -> syncId) })
    val res = stmt.execute()
    stmt.close()
    res
  }

  override def resultSetToObject(result: ResultSet): Person = {
    Person(
      result.getString("person_id"),
      result.getInt("birth_year"),
      result.getInt("gender"),
      result.getString("first_name"),
      result.getString("last_name"),
      result.getString("status"),
      result.getString("party")
    )
  }

  def latest(): Seq[Person] = {
    val stmt = db.prepareStatement(
      s"""
         |select *
         |from person t1
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
