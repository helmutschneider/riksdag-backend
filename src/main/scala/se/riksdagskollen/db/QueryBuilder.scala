package se.riksdagskollen.db

import java.sql.{Connection, PreparedStatement, Statement}

/**
  * Created by johan on 2016-05-30.
  */
class QueryBuilder(db: Connection) {

  def insert(tableName: String, dataMap: Map[String, Any]): PreparedStatement = {
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
    stmt
  }

  def insertAll(tableName: String, dataMap: Seq[Map[String, Any]]): PreparedStatement = {
    val columnNames = dataMap.head.keys.mkString(",")
    val qstn = dataMap.head map { kv => "?" } mkString ","
    val placeHolders = dataMap map { it => s"($qstn)" } mkString ","
    val stmt = db.prepareStatement(
      s"insert into $tableName ($columnNames) values $placeHolders",
      Statement.RETURN_GENERATED_KEYS
    )

    var i = 1
    dataMap foreach { row =>
      row.values foreach { v =>
        stmt.setObject(i, v)
        i += 1
      }
    }

    stmt
  }

  def update(tableName: String, dataMap: Map[String, Any], where: Map[String, Any]): PreparedStatement = {
    val columnNames = dataMap.keys map { k => s"$k = ?" } mkString ","
    val wherePart = where.keys map { k => s"$k = ?" } mkString " and "
    val stmt = db.prepareStatement(s"update $tableName set $columnNames where $wherePart")
    var i = 1
    dataMap.values foreach { v =>
      stmt.setObject(i, v)
      i += 1
    }
    where.values foreach { v =>
      stmt.setObject(i, v)
      i += 1
    }
    stmt
  }

}
