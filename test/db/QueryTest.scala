package db

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by johan on 15-08-27.
 */
class QueryTest extends FunSuite with BeforeAndAfter {

  var q: Query = null

  before {
    q = new Query
  }

  test("select") {
    val sql = q.select("one", "two", "three").sql
    assert(sql == "select one,two,three")
  }

  test("from") {
    val sql = q.from("some-table").sql

    assert(sql == "from some-table")
  }

  test("join") {
    val sql = q.join("other-table o", "o.id = b.id", db.Query.Join.Inner).sql

    assert(sql == "inner join other-table o on o.id = b.id")
  }

  test("multiple table join") {
    val sql = q.join("table1 t1", "t1.id = b.id", db.Query.Join.Inner)
      .join("table2 t2", "t2.id = t1.id", db.Query.Join.Left)
      .sql

    assert(sql == "inner join table1 t1 on t1.id = b.id left join table2 t2 on t2.id = t1.id")
  }

  test("single where") {
    val sql = q.where("car.id = 5", db.Query.Where.And).sql

    assert(sql == "where car.id = 5")
  }

  test("multiple where") {
    val sql = q.where("car.id = 5", db.Query.Where.And)
      .where("car.brand = 'volvo'", db.Query.Where.And)
      .where("car.model = 'v70'", db.Query.Where.Or)
      .sql

    assert(sql == "where car.id = 5 and car.brand = 'volvo' or car.model = 'v70'")
  }

  test("group by") {
    val sql = q.groupBy("name", "age").sql

    assert(sql == "group by name,age")
  }

  test("order by") {
    val sql = q.orderBy("name", Query.Sort.Ascending).sql

    assert(sql == "order by name asc")
  }

  test("builds complex query") {
    val sql = q.select ("one", "two")
      .from ("tbl")
      .where ("one = 5", db.Query.Where.And)
      .join ("tbl2", "tbl2.id = tbl.id", db.Query.Join.Inner)
      .limit (5)
      .offset (10)
      .orderBy ("cars", db.Query.Sort.Ascending)
      .groupBy ("cars")
      .sql

    assert(sql == "select one,two from tbl inner join tbl2 on tbl2.id = tbl.id where one = 5 group by cars order by cars asc limit 5 offset 10")
  }

  test("from subquery") {
    val q2 = (new Query)
      .select("a", "b")
      .from("tbl2")

    val sql = q.select("*")
      .from(q2, "q2")
      .sql

    assert(sql == "select * from (select a,b from tbl2) q2")
  }

  test("join subquery") {
    val q2 = (new Query)
      .select("a", "b")
      .from("tbl2")

    val sql = q.select("*")
    .from("tbl")
    .join(q2, "q2", "q2.id = tbl.id", db.Query.Join.Inner)
    .sql

    println(sql)

    assert(sql == "select * from tbl inner join (select a,b from tbl2) q2 on q2.id = tbl.id")
  }

  test("where with subquery") {
    val q2 = q.select("max(id)").from("cars")
    val sql = q.where("id", q2, db.Query.Where.And).sql

    assert(sql == "where id=(select max(id) from cars)")
  }

  test("multiple order by") {
    val sql = q.orderBy("car", db.Query.Sort.Ascending).orderBy("boat", db.Query.Sort.Descending).sql

    assert(sql == "order by car asc,boat desc")
  }

}
