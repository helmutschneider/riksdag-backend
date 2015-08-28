package db

import db.Query.Join
import db.Query.Where
import db.Query.Sort

import scala.collection.immutable.ListMap

/**
 * Created by johan on 15-08-27.
 */

object Query {

  object Join extends Enumeration {
    type Join = Value

    val Inner = Value(0)
    val Left = Value(1)
  }

  object Where extends Enumeration {
    type Where = Value

    val And = Value(0)
    val Or = Value(1)
  }

  object Sort extends Enumeration {
    type Sort = Value

    val Descending = Value(0)
    val Ascending = Value(1)
  }

}

class Query(pts: Map[String, Seq[String]] = Map()) {

  private val templates = ListMap(
    "select" -> "select %s",
    "from" -> "from %s",
    "join" -> "%s",
    "where" -> "where %s",
    "groupBy" -> "group by %s",
    "orderBy" -> "order by %s",
    "limit" -> "limit %s",
    "offset" -> "offset %s"
  )

  private val parts: Map[String, Seq[String]] = templates map { kv =>
    val injected = pts.get(kv._1)

    injected match {
      case Some(x) => kv._1 -> x
      case _ => kv._1 -> List()
    }
  }

  def select(columns: String *): Query = {
    new Query(parts.updated("select", List(columns.mkString(","))))
  }

  def from(tables: String *): Query = {
    new Query(parts.updated("from", tables))
  }

  def from(query: Query, alias: String): Query = {
    this.from("(" + query.sql + ") " + alias)
  }

  /**
   * 
   * @param table Table to join
   * @param criteria sql boolean expression. The on-part of the join expression.
   * @param joinType inner/left
   * @return
   */
  def join(table: String, criteria: String, joinType: Join.Join) : Query = {
    val t = joinType match {
      case Join.Left => "left"
      case Join.Inner => "inner"
    }

    val old = parts("join")
    new Query(parts.updated("join", old :+ s"$t join $table on $criteria"))
  }

  def join(query: Query, alias: String, on: String, joinType: Join.Join): Query = {
    this.join("(" + query.sql + ") " + alias, on, joinType)
  }

  /**
   *
   * @param criteria sql boolean expression
   * @param whereType and/or
   * @return
   */
  def where(criteria: String, whereType: Where.Where = Where.And): Query = {
    val t = whereType match {
      case Where.And => "and"
      case Where.Or => "or"
    }

    val old = parts("where")
    val str = old.isEmpty match {
      case true => criteria
      case false => t + " " + criteria
    }

    new Query(parts.updated("where", old :+ str))
  }

  def groupBy(columns: String *): Query = {
    new Query(parts.updated("groupBy", List(columns.mkString(","))))
  }

  def orderBy(column: String, sort: Sort.Sort): Query = {
    val s = sort match {
      case Sort.Ascending => "asc"
      case Sort.Descending => "desc"
    }

    new Query(parts.updated("orderBy", List(column + " " + s)))
  }

  def limit(num: Int): Query = {
    new Query(parts.updated("limit", List(num.toString)))
  }

  def offset(num: Int): Query = {
    new Query(parts.updated("offset", List(num.toString)))
  }

  def sql: String = {

    val s = templates map { kv =>
      val pts = parts(kv._1)
      pts.isEmpty match {
        case false => kv._2.format(pts.mkString(" "))
        case true => ""
      }
    }
    s filter (!_.isEmpty) mkString " "
  }

}
