package db

import db.Query.Join
import db.Query.Where
import db.Query.Sort

import scala.collection.immutable.ListMap
import scala.collection.mutable

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

class Query {

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

  private val parts = mutable.ListMap(
    (templates map { kv => kv._1 -> List[String]() }).toSeq: _*
  )

  def select(columns: Seq[String]): Query = {

    parts.update("select", List(columns.mkString(",")))
    this
  }

  def from(table: String): Query = {
    parts.update("from", List(table))
    this
  }

  def from(query: Query, alias: String): Query = {
    this.from("(" + query.sql + ") " + alias)
  }

  def join(table: String, on: String, joinType: Join.Join = Join.Inner) : Query = {
    val t = joinType match {
      case Join.Left => "left"
      case Join.Inner => "inner"
    }

    val old = parts("join")
    parts.update("join", old :+ s"$t join $table on $on")

    this
  }

  def join(query: Query, alias: String, on: String, joinType: Join.Join): Query = {
    this.join("(" + query.sql + ") " + alias, on, joinType)
  }

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

    parts.update("where", old :+ str)
    this
  }

  def groupBy(columns: Seq[String]): Query = {
    parts.update("groupBy", List(columns.mkString(",")))
    this
  }

  def orderBy(column: String, sort: Sort.Sort): Query = {
    val s = sort match {
      case Sort.Ascending => "asc"
      case Sort.Descending => "desc"
    }

    parts.update("orderBy", List(column + " " + s))
    this
  }

  def limit(num: Int): Query = {
    parts.update("limit", List(num.toString))
    this
  }

  def offset(num: Int): Query = {
    parts.update("offset", List(num.toString))
    this
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
