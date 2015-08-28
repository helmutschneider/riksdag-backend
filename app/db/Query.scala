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

class Query(pts: Map[String, String] = Map()) {

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

  private val parts: Map[String, String] = templates map { kv =>
    val injected = pts.get(kv._1)

    injected match {
      case Some(x) => kv._1 -> x
      case _ => kv._1 -> ""
    }
  }

  def select(columns: String *): Query = {
    new Query(parts.updated("select", columns.mkString(",")))
  }

  def from(tables: String *): Query = {
    new Query(parts.updated("from", tables.mkString(",")))
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
      case Join.Left => "left join"
      case Join.Inner => "inner join"
    }

    val old = parts("join")
    new Query(parts.updated("join", List(old, t + " " + table + " on " + criteria) filter (!_.isEmpty) mkString " " ))
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
  def where(criteria: String, whereType: Where.Where): Query = {
    val t = whereType match {
      case Where.And => "and"
      case Where.Or => "or"
    }

    val old = parts("where")
    val str = old.isEmpty match {
      case true => criteria
      case false => t + " " + criteria
    }

    new Query(parts.updated("where", List(old, str) filter (!_.isEmpty) mkString " " ))
  }

  def groupBy(columns: String *): Query = {
    new Query(parts.updated("groupBy", columns.mkString(",") ))
  }

  def orderBy(column: String, sort: Sort.Sort): Query = {
    val s = sort match {
      case Sort.Ascending => "asc"
      case Sort.Descending => "desc"
    }

    new Query(parts.updated("orderBy", column + " " + s))
  }

  def limit(num: Int): Query = {
    new Query(parts.updated("limit", num.toString))
  }

  def offset(num: Int): Query = {
    new Query(parts.updated("offset", num.toString))
  }

  def sql: String = {
    parts filter (!_._2.isEmpty) map { kv =>
      templates(kv._1).format(kv._2)
    } mkString " "
  }

}
