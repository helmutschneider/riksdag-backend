package se.riksdagskollen.db

import java.sql.{Connection, PreparedStatement, Statement, Timestamp}

import se.riksdagskollen.app.{Person, Sync, Vote, Voting}

trait Repository[PKType, T] {
  def insert(data: T): PKType
  def update(data: T, id: PKType): Boolean
  //def delete(id: PKType): Boolean
  //def one(id: PKType): Option[T]
  //def all(): Seq[T]
}

class PersonRepository(db: Connection, syncId: Int) extends Repository[(String, Int), Person] {
  val builder = new QueryBuilder(db)
  override def insert(data: Person): (String, Int) = {
    val stmt = builder.insert("person", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    stmt.close()
    (data.id, syncId)
  }

  override def update(data: Person, id: (String, Int)): Boolean = ???
}

class SyncRepository(db: Connection) extends Repository[Int, Sync] {
  val builder = new QueryBuilder(db)
  override def insert(data: Sync): Int = {
    val stmt = builder.insert("sync", data.toMap)
    stmt.execute()
    val keys = stmt.getGeneratedKeys
    keys.next()
    val id = keys.getInt(1)
    keys.close()
    stmt.close()
    id
  }

  override def update(data: Sync, id: Int): Boolean = {
    val stmt = builder.update("sync", data.toMap, Map("sync_id" -> id))
    val res = stmt.execute()
    stmt.close()
    res
  }

}

class VotingRepository(db: Connection, syncId: Int) extends Repository[(String, Int), Voting] {
  val builder = new QueryBuilder(db)

  override def insert(data: Voting): (String, Int) = {
    val stmt = builder.insert("voting", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    (data.id, syncId)
  }

  override def update(data: Voting, id: (String, Int)): Boolean = ???
}

class VoteRepository(db: Connection, syncId: Int) extends Repository[(String, String, Int), Vote] {
  val builder = new QueryBuilder(db)

  override def insert(data: Vote): (String, String, Int) = {
    val stmt = builder.insert("vote", data.toMap + ("sync_id" -> syncId))
    stmt.execute()
    (data.votingId, data.personId, syncId)
  }

  override def update(data: Vote, id: (String, String, Int)): Boolean = ???
}