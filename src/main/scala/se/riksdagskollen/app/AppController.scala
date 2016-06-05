package se.riksdagskollen.app

import org.json4s.{DefaultFormats, Formats}
import se.riksdagskollen.http.{PersonRepository, SyncRunner}
import se.riksdagskollen.db

import scala.concurrent.ExecutionContext

class AppController(app: Application) extends Servlet {

  override implicit lazy val jsonFormats = DefaultFormats ++ Seq(
    PersonRepository.serializer
  )

  implicit val context = ExecutionContext.global
  val httpClient = app.httpClient
  val dataSource = app.dataSource

  get("/") {
    Map("version" -> app.version)
  }

  get("/person") {
    val conn = dataSource.getConnection
    val personRepo = new db.PersonRepository(conn)
    val res = personRepo.latest()
    conn.close()
    res
  }

  get("/person/status") {
    val conn = dataSource.getConnection
    val personRepo = new db.PersonRepository(conn)
    val res = personRepo.statuses()
    conn.close()
    res
  }

  get("/person/birth-year") {
    val conn = dataSource.getConnection
    val personRepo = new db.PersonRepository(conn)
    val res = personRepo.birthYears()
    conn.close()
    res
  }

  get("/person/gender") {
    val conn = dataSource.getConnection
    val personRepo = new db.PersonRepository(conn)
    val res = personRepo.genders()
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
        |and s.completed_at is null
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
    val res = votingRepo.latest()
    conn.close()
    res
  }

}
