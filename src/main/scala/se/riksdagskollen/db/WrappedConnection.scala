package se.riksdagskollen.db

import java.sql.PreparedStatement
import java.sql.{Connection}
import scala.util.matching.Regex

class WrappedConnection(wrapped: Connection) {

  def prepare(sql: String, params: Map[String, Any] = Map()): PreparedStatement = {
    val regex = new Regex(":(\\w+)", "name")
    val stmt = wrapped.prepareStatement(regex.replaceAllIn(sql, "?"))
    regex.findAllMatchIn(sql).zipWithIndex.foreach { m =>
      val value = params(m._1.group("name"))
      stmt.setObject(m._2 + 1, value)
    }
    stmt
  }

  def queryAll(sql: String, params: Map[String, Any] = Map()): Seq[Map[String, Any]] = {
    val stmt = prepare(sql, params)
    val res = stmt.executeQuery()
    val meta = res.getMetaData
    val columns = (1 to meta.getColumnCount).map { meta.getColumnName }
    val builder = Seq.newBuilder[Map[String, Any]]
    while (res.next()) {
      builder += (columns map { name =>
        name -> res.getObject(name)
      }).toMap
    }
    res.close()
    stmt.close()
    builder.result()
  }

  def query(sql: String, params: Map[String, Any] = Map()): Option[Map[String, Any]] = {
    queryAll(sql, params).headOption
  }

  def execute(sql: String, params: Map[String, Any]): Boolean = {
    val stmt = prepare(sql, params)
    val res = stmt.execute()
    stmt.close()
    res
  }

}
