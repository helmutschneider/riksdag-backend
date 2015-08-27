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
    q.select(List("one", "two", "three"))

    assert(q.sql == "select one,two,three")
  }

  test("from") {
    q.from("some-table")

    assert(q.sql == "from some-table")
  }

  test("join") {
    q.join("other-table o", "o.id = b.id", db.Query.Join.Inner)

    assert(q.sql == "inner join other-table o on o.id = b.id")
  }

  test("multiple table join") {
    q.join("table1 t1", "t1.id = b.id", db.Query.Join.Inner)
    q.join("table2 t2", "t2.id = t1.id", db.Query.Join.Left)

    assert(q.sql == "inner join table1 t1 on t1.id = b.id left join table2 t2 on t2.id = t1.id")
  }

  test("single where") {
    q.where("car.id = 5", db.Query.Where.And)

    assert(q.sql == "where car.id = 5")
  }

  test("multiple where") {
    q.where("car.id = 5", db.Query.Where.And)
    q.where("car.brand = 'volvo'", db.Query.Where.And)
    q.where("car.model = 'v70'", db.Query.Where.Or)

    assert(q.sql == "where car.id = 5 and car.brand = 'volvo' or car.model = 'v70'")
  }

  test("group by") {
    q.groupBy(List("name", "age"))

    assert(q.sql == "group by name,age")
  }

  test("order by") {
    q.orderBy("name", Query.Sort.Ascending)

    assert(q.sql == "order by name asc")
  }

  test("builds complex query") {
    q.select (List("one", "two"))
      .from ("tbl")
      .where ("one = 5", db.Query.Where.And)
      .join ("tbl2", "tbl2.id = tbl.id", db.Query.Join.Inner)
      .limit (5)
      .offset (10)
      .orderBy ("cars", db.Query.Sort.Ascending)
      .groupBy (List("cars"))

    assert(q.sql == "select one,two from tbl inner join tbl2 on tbl2.id = tbl.id where one = 5 group by cars order by cars asc limit 5 offset 10")
  }

  test("from subquery") {
    val q2 = (new Query)
      .select(List("a", "b"))
      .from("tbl2")

    q.select(List("*"))
      .from(q2, "q2")

    assert(q.sql == "select * from (select a,b from tbl2) q2")
  }

  test("join subquery") {
    val q2 = (new Query)
      .select(List("a", "b"))
      .from("tbl2")

    q.select(List("*"))
    .from("tbl")
    .join(q2, "q2", "q2.id = tbl.id", db.Query.Join.Inner)

    println(q.sql)

    assert(q.sql == "select * from tbl inner join (select a,b from tbl2) q2 on q2.id = tbl.id")
  }

}
