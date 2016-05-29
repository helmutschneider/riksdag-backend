package se.riksdagskollen.db

import java.sql.{Connection, PreparedStatement, Statement, Timestamp}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import se.riksdagskollen.app.{DatabaseModel, Person, Sync}

abstract class Repository[T <: DatabaseModel](db: Connection) {

  def tableName: String
  def primaryKeyName: String
  def toObject(data: Map[String, String]): T
  def toMap(obj: T): Map[String, String]

  def insert(obj: T): T = {
    val dataMap = toMap(obj)
    val columnNames = dataMap.keys.mkString(",")
    val placeholders = dataMap map { kv => "?" } mkString ","
    val stmt = db.prepareStatement(
      s"insert into $tableName($columnNames) values($placeholders)",
      Statement.RETURN_GENERATED_KEYS
    )
    var i = 1
    dataMap.values foreach { v =>
      stmt.setObject(i, v)
      i += 1
    }
    stmt.execute()
    val keys = stmt.getGeneratedKeys
    keys.next()
    val res = obj.withDatabaseId(keys.getInt(1)).asInstanceOf[T]
    keys.close()
    stmt.close()
    res
  }

  def update(obj: T): T = {
    val dataMap = toMap(obj)
    val columnNames = dataMap.keys map { k => s"$k = ?" } mkString ","
    val stmt = db.prepareStatement(s"update $tableName set $columnNames where $primaryKeyName = ?")

    var i = 1
    dataMap.values foreach { v =>
      stmt.setObject(i, v)
      i += 1
    }
    stmt.setInt(i, obj.databaseId.get.toInt)
    stmt.execute()
    stmt.close()
    obj
  }

  def delete(id: Int): Boolean = {
    val stmt = db.prepareStatement(s"delete from $tableName where $primaryKeyName = ?")
    stmt.setInt(1, id)
    val res = stmt.execute()
    stmt.close()
    res
  }

  def select(stmt: PreparedStatement): List[T] = {
    val result = stmt.executeQuery()
    val meta = result.getMetaData
    val names = (1 to meta.getColumnCount) map { meta.getColumnName }
    val builder = List.newBuilder[T]
    while (result.next()) {
      val data = (names map { key =>
        (key, result.getString(key))
      }).toMap
      builder += toObject(data)
    }
    result.close()
    stmt.close()
    builder.result()
  }

  def all(): List[T] = {
    val stmt = db.prepareStatement(s"select * from $tableName")
    select(stmt)
  }

  def one(id: Int): Option[T] = {
    val stmt = db.prepareStatement(s"select * from $tableName where $primaryKeyName = ?")
    stmt.setInt(1, id)
    select(stmt).headOption
  }

}

object Repository {

  def forPerson(db: Connection): Repository[Person] = {
    new Repository[Person](db) {
      override def tableName = "person"
      override def primaryKeyName = "person_id"
      override def toMap(obj: Person) = {
        Map[String, String](
          "person_id" -> (obj.databaseId match {
            case Some(x: BigInt) => x.toInt.toString
            case _ => null
          }),
          "remote_id" -> obj.id,
          "birth_year" -> obj.birthYear.toString,
          "gender" -> obj.gender,
          "first_name" -> obj.firstName,
          "last_name" -> obj.lastName,
          "party" -> obj.party,
          "location" -> obj.location,
          "status" -> obj.status,
          "sync_id" -> (obj.syncId match {
            case Some(x: BigInt) => x.toInt.toString
            case _ => null
          })
        )
      }
      def toObject(data: Map[String, String]): Person = {
        Person(
          data("remote_id"),
          data("birth_year").toInt,
          data("gender"),
          data("first_name"),
          data("last_name"),
          data("status"),
          data("party"),
          data("location"),
          Some(data("person_id").toInt),
          Some(data("sync_id").toInt)
        )
      }
    }
  }

  def forSync(db: Connection): Repository[Sync] = {
    new Repository[Sync](db) {
      override def tableName = "sync"
      override def primaryKeyName = "sync_id"
      override def toMap(obj: Sync) = {
        Map[String, String](
          "sync_id" -> (obj.databaseId match {
            case Some(x: BigInt) => x.toInt.toString
            case _ => null
          }),
          "started_at" -> obj.startedAt.toString,
          "completed_at" -> (obj.completedAt match {
            case Some(x) => x.toString
            case _ => null
          })
        )
      }
      override def toObject(data: Map[String, String]): Sync = {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dt = ZonedDateTime.parse(data("started_at"), formatter)
        val s = Sync(
          new Timestamp(dt.toInstant.getEpochSecond * 1000),
          data("completed_at") match {
            case x: String =>
              val d = ZonedDateTime.parse(x, formatter)
              Some(new Timestamp(d.toInstant.getEpochSecond * 1000))
            case _ => None
          },
          Some(data("sync_id").toInt)
        )
        s
      }
    }
  }

}