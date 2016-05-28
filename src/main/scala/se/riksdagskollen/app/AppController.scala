package se.riksdagskollen.app

import java.sql.Connection
import java.util.concurrent.{Executors, TimeUnit}

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{AsyncResult, FutureSupport}
import se.riksdagskollen.db.Repository
import se.riksdagskollen.http.ScalajHttpClient

import scala.concurrent.{ExecutionContext, Future}

class AppController(db: Connection) extends Servlet with FutureSupport {

  override implicit lazy val jsonFormats = DefaultFormats + PersonRepository.serializer

  implicit val context = ExecutionContext.global
  val executor = context
  val httpClient = new ScalajHttpClient(ExecutionContext.global)
  val repo = new PersonRepository(httpClient, ExecutionContext.global)

  get("/") {
    Map("Hello" -> "World")
  }

  get("/person") {
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
    personRepo.select(stmt)
  }

  get("/sync") {
    val syncer = new SyncRunner(db, httpClient, context)
    println("Syncing...")
    new AsyncResult() {
      val is = syncer.run() map { res =>
        println("Done!")
        res
      }
    }
  }

}
