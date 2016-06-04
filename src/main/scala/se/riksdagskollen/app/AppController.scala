package se.riksdagskollen.app

import java.sql.Connection
import java.util.concurrent.{Executors, TimeUnit}
import javax.sql.DataSource

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{AsyncResult, FutureSupport}
import se.riksdagskollen.db.Repository
import se.riksdagskollen.http.{PersonRepository, ScalajHttpClient, SyncRunner, VotingRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AppController(dataSource: DataSource) extends Servlet with FutureSupport {

  override implicit lazy val jsonFormats = DefaultFormats ++ Seq(
    PersonRepository.serializer
  )
  options("/*"){
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }

  implicit val context = ExecutionContext.global
  val executor = context
  val httpClient = new ScalajHttpClient(ExecutionContext.global)
  val repo = new PersonRepository(httpClient, ExecutionContext.global)

  get("/") {
    Map("Hello" -> "World")
  }

  get("/person") {
    val repo = new PersonRepository(httpClient, context)

    new AsyncResult() {
      val is = repo.fetch()
    }

    /*
    val db = dataSource.getConnection
    val personRepo = Repository.forPerson(db)
    val stmt = db.prepareStatement(
      s"""
        |select *
        |from ${personRepo.tableName} t1
        |inner join (
        |   select max(sync_id) as sync_id
        |   from sync
        |   where completed_at is not null
        |) t2
        |on t1.sync_id = t2.sync_id
      """.stripMargin)
    val res = personRepo.select(stmt)
    db.close()
    res
    */
  }

  get("/sync") {
    val db = dataSource.getConnection
    val executor = Executors.newScheduledThreadPool(1)
    executor.schedule(new Runnable {
      override def run(): Unit = {
        val syncer = new SyncRunner(db, httpClient, context)
        syncer.run()
      }
    }, 0, TimeUnit.SECONDS)
    Seq("status" -> "started")
  }

  get("/voting") {
    val repo = new VotingRepository(httpClient, context)
    new AsyncResult() {
      val is = repo.fetchVotingIds()
    }
  }

}
