package se.riksdagskollen.app

import org.json4s.{DefaultFormats}
import se.riksdagskollen.http.{PersonRepository, ScalajHttpClient, SyncRunner}
import se.riksdagskollen.db
import se.riksdagskollen.db.WrappedConnection

class AppController(app: Application) extends Servlet {

  override implicit lazy val jsonFormats = DefaultFormats ++ Seq(
    PersonRepository.serializer
  )

  implicit val context = app.executionContext
  val dataSource = app.dataSource

  get("/") {
    Map("version" -> app.version)
  }

  get("/person") {
    val conn = dataSource.getConnection
    val repo = new db.PersonRepository(conn)
    val fetchAll = request.getParameter("all")
    val res = if (fetchAll == null) repo.latestActive() else repo.latestAll()
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

  get("/party") {
    val conn = dataSource.getConnection
    val repo = new db.PersonRepository(conn)
    val res = repo.parties()
    conn.close()
    res
  }

  get("/sync") {
    val sql =
      """
        |select
        | s.*,
        | coalesce(t1.person_count, 0) as person_count,
        | coalesce(t2.voting_count, 0) as voting_count
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
      """.stripMargin

    val conn = dataSource.getConnection
    val wrapped = new WrappedConnection(conn)
    val data = wrapped.query(sql)

    // check if a sync is running and start one if it is not.
    data match {
      case Some(sync) =>
        conn.close()
        sync
      case _ =>
        val httpClient = new ScalajHttpClient(app.executionContext)
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
