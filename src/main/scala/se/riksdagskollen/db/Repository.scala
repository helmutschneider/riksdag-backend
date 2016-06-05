package se.riksdagskollen.db

import java.sql._

import se.riksdagskollen.app.{Person, Sync, Vote, Voting}

trait Repository[T] {
  private def select(statement: PreparedStatement): Seq[T] = {
    val result = statement.executeQuery()
    val builder = Seq.newBuilder[T]
    while (result.next()) {
      builder += resultSetToObject(result)
    }
    result.close()
    builder.result()
  }

  def all(statement: PreparedStatement): Seq[T] = {
    select(statement)
  }

  def one(statement: PreparedStatement): Option[T] = {
    select(statement).headOption
  }

  def resultSetToObject(result: ResultSet): T
}

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
}

class SyncRepository(db: Connection) extends Repository[Sync] {
  val builder = new QueryBuilder(db)

  def insert(data: Sync): Int = {
    val stmt = builder.insert("sync", data.toMap)
    stmt.execute()
    val keys = stmt.getGeneratedKeys
    keys.next()
    val id = keys.getInt(1)
    keys.close()
    stmt.close()
    id
  }

  def update(data: Sync, id: Int): Boolean = {
    val stmt = builder.update("sync", data.toMap, Map("sync_id" -> id))
    val res = stmt.execute()
    stmt.close()
    res
  }

  override def resultSetToObject(result: ResultSet): Sync = {
    Sync(
      result.getTimestamp("started_at"),
      Some(result.getTimestamp("completed_at"))
    )
  }
}

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
      result.getTimestamp("date")
    )
  }
}

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