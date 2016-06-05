package se.riksdagskollen.db

import java.sql.{Connection}

import se.riksdagskollen.app.Person

class PersonRepository(db: Connection) {
  val builder = new QueryBuilder(db)
  val wrapped = new WrappedConnection(db)

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

  def mapToObject(data: Map[String, Any]): Person = {
    Person(
      data("person_id").asInstanceOf[String],
      data("birth_year").asInstanceOf[Int],
      data("gender").asInstanceOf[Int],
      data("first_name").asInstanceOf[String],
      data("last_name").asInstanceOf[String],
      data("status").asInstanceOf[String],
      data("party").asInstanceOf[String]
    )
  }

  def latest(): Seq[Person] = {
    val sql =
      """
         |select *
         |from person
         |where sync_id = (
         |   select max(sync_id) as sync_id
         |   from sync
         |   where completed_at is not null
         |)
      """.stripMargin

    wrapped.queryAll(sql) map { mapToObject }
  }

  def statuses(): Seq[Map[String, Any]] = {
    val sql =
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

    wrapped.queryAll(sql)
  }

  def birthYears(): Seq[Map[String, Any]] = {
    val sql =
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

    wrapped.queryAll(sql)
  }

  def genders(): Seq[Map[String, Any]] = {
    val sql =
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

    wrapped.queryAll(sql)
  }

  def parties(): Seq[Map[String, Any]] = {
    val sql =
      """
        |select
        | party,
        | count(party) as count
        |from person
        |where sync_id = (
        | select max(sync_id)
        | from sync
        | where completed_at is not null
        |)
        |group by party
        |order by party
      """.stripMargin

    wrapped.queryAll(sql)
  }

}
