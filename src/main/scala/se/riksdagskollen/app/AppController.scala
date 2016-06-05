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
    println(dataSource)
    val conn = dataSource.getConnection
    println(conn)
    val repo = new db.SyncRepository(conn)
    val stmt = conn.prepareStatement(
      """
        |select *
        |from sync
        |where sync_id = (
        | select max(sync_id)
        | from sync
        |)
        |and completed_at is null
      """.stripMargin)

    val res = repo.one(stmt)
    stmt.close()

    // check if a sync is running and start one if it is not.
    res match {
      case Some(sync) => sync
      case _ =>
        val syncer = new SyncRunner(conn, httpClient, context)
        val res = syncer.run()
        res._1
    }
  }

  get("/voting") {
    val repo = new VotingRepository(httpClient, context)
    new AsyncResult() {
      val is = repo.fetchVotingIds()
    }
  }

}
