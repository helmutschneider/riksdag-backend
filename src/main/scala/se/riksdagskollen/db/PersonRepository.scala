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
      """
         |select *
         |from person
         |where sync_id = (
         |   select max(sync_id) as sync_id
         |   from sync
         |   where completed_at is not null
         |)
      """.stripMargin)
    val res = select(stmt)
    stmt.close()
    res
  }

  def statuses(): Seq[Map[String, Any]] = {
    val stmt = db.prepareStatement(
      """
        |select
        | status,
        | count(status) as count
        |from person
        |where sync_id = (
        | select max(sync_id)
        | from sync
        | where completed_at is not null
        |)
        |group by status
      """.stripMargin
    )
    val res = stmt.executeQuery()
    val builder = Seq.newBuilder[Map[String, Any]]
    while (res.next()) {
      builder += Map(
        "status" -> res.getString("status"),
        "count" -> res.getInt("count")
      )
    }
    res.close()
    stmt.close()
    builder.result()
  }

  def birthYears(): Seq[Map[String, Any]] = {
    val stmt = db.prepareStatement(
      """
        |select
        | birth_year,
        | count(birth_year) as count
        |from person
        |where sync_id = (
        | select max(sync_id)
        | from sync
        | where completed_at is not null
        |)
        |group by birth_year
        |order by birth_year
      """.stripMargin
    )

    val res = stmt.executeQuery()
    val builder = Seq.newBuilder[Map[String, Any]]
    while (res.next()) {
      builder += Map(
        "birth_year" -> res.getInt("birth_year"),
        "count" -> res.getInt("count")
      )
    }
    res.close()
    stmt.close()
    builder.result()
  }

  def genders(): Seq[Map[String, Any]] = {
    val stmt = db.prepareStatement(
      """
        |select
        | gender,
        | count(gender) as count
        |from person
        |where sync_id = (
        | select max(sync_id)
        | from sync
        | where completed_at is not null
        |)
        |group by gender
        |order by gender
      """.stripMargin
    )

    val res = stmt.executeQuery()
    val builder = Seq.newBuilder[Map[String, Any]]
    while (res.next()) {
      builder += Map(
        "gender" -> res.getInt("gender"),
        "count" -> res.getInt("count")
      )
    }
    res.close()
    stmt.close()
    builder.result()
  }

}
