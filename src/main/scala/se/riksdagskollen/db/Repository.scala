package se.riksdagskollen.db

import java.sql._

trait Repository[T] {
  protected def select(statement: PreparedStatement): Seq[T] = {
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
