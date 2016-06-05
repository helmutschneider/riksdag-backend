package se.riksdagskollen.app

import java.sql.{Connection, Timestamp}
import java.util.concurrent.{Executors, TimeUnit}
import javax.sql.DataSource

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{AsyncResult, FutureSupport}
import se.riksdagskollen.http.{PersonRepository, ScalajHttpClient, SyncRunner, VotingRepository}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import se.riksdagskollen.db
import se.riksdagskollen.db.Repository

class AppController(dataSource: DataSource) extends Servlet with FutureSupport {

  override implicit lazy val jsonFormats = DefaultFormats ++ Seq(
    PersonRepository.serializer
  )

  implicit val context = ExecutionContext.global
  val executor = context
  val httpClient = new ScalajHttpClient(ExecutionContext.global)
  val repo = new PersonRepository(httpClient, ExecutionContext.global)

  get("/") {
    Map("Hello" -> "World")
  }

  get("/person") {
    val conn = dataSource.getConnection
    val personRepo = new db.PersonRepository(conn)
    val stmt = conn.prepareStatement(
      s"""
        |select *
        |from person t1
        |inner join (
        |   select max(sync_id) as sync_id
        |   from sync
        |   where completed_at is not null
        |) t2
        |on t1.sync_id = t2.sync_id
      """.stripMargin)
    val res = personRepo.all(stmt)
    stmt.close()
    conn.close()
    res
  }

  get("/sync") {
    val conn = dataSource.getConnection
    val stmt = conn.prepareStatement(
      """
        |select
        | s.*,
        | t1.*,
        | t2.*
        |from sync as s
        |left join (
        | select count(person_id) as person_count, sync_id
        | from person
        | group by sync_id
        |) t1
        |on t1.sync_id = s.sync_id
        |left join (
        | select count(voting_id) as voting_count, sync_id
        | from voting
        | group by sync_id
        |) t2
        |on t2.sync_id = s.sync_id
        |where s.sync_id = (
        | select max(sync_id)
        | from sync
        |)
      """.stripMargin)

    //val res = repo.one(stmt)
    val res = stmt.executeQuery()
    val builder = Seq.newBuilder[Map[String, Any]]

    while (res.next()) {
      builder += Map(
        "started_at" -> res.getTimestamp("started_at"),
        "completed_at" -> res.getTimestamp("completed_at"),
        "person_count" -> res.getInt("person_count"),
        "voting_count" -> res.getInt("voting_count")
      )
    }

    res.close()
    stmt.close()

    // check if a sync is running and start one if it is not.
    builder.result().headOption match {
      case Some(sync) =>
        conn.close()
        sync
      case _ =>
        val syncer = new SyncRunner(conn, httpClient, context)
        val res = syncer.run()
          res._2 map { res =>
          conn.close()
        }
        res._1
    }
  }

  get("/voting") {
    val conn = dataSource.getConnection
    val votingRepo = new db.VotingRepository(conn)
    val stmt = conn.prepareStatement(
      s"""
         |select *
         |from voting t1
         |inner join (
         |   select max(sync_id) as sync_id
         |   from sync
         |   where completed_at is not null
         |) t2
         |on t1.sync_id = t2.sync_id
      """.stripMargin)
    val res = votingRepo.all(stmt)
    stmt.close()
    conn.close()
    res
  }

}
