package se.riksdagskollen.db

import java.sql.{Connection, PreparedStatement, Statement, Timestamp}

import se.riksdagskollen.app.{Person, Sync, Vote, Voting}

class PersonRepository(db: Connection) {
  val builder = new QueryBuilder(db)

  def insert(data: Person, syncId: Int): (String, Int) = {
    val stmt = builder.insert("person", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    stmt.close()
    (data.id, syncId)
  }

  def update(data: Person, syncId: Int): Boolean = ???

  private def select(statement: PreparedStatement): Seq[Person] = {
    val result = statement.executeQuery()
    val builder = Seq.newBuilder[Person]
    while (result.next()) {
      builder += Person(
        result.getString("person_id"),
        result.getInt("birth_year"),
        result.getString("gender"),
        result.getString("first_name"),
        result.getString("last_name"),
        result.getString("status"),
        result.getString("party")
      )
    }
    result.close()
    builder.result()
  }

  def all(statement: PreparedStatement): Seq[Person] = {
    select(statement)
  }

  def one(statement: PreparedStatement): Person = {
    select(statement).head
  }
}

class SyncRepository(db: Connection) {
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

}

class VotingRepository(db: Connection) {
  val builder = new QueryBuilder(db)

  def insert(data: Voting, syncId: Int): (String, Int) = {
    val stmt = builder.insert("voting", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    (data.id, syncId)
  }

  def update(data: Voting, id: (String, Int)): Boolean = ???
}

class VoteRepository(db: Connection) {
  val builder = new QueryBuilder(db)

  def insert(data: Vote, syncId: Int): (String, String, Int) = {
    val stmt = builder.insert("vote", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    (data.votingId, data.personId, syncId)
  }

  def update(data: Vote, id: (String, String, Int)): Boolean = ???
}