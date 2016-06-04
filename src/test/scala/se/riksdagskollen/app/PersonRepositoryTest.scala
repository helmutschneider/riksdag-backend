package se.riksdagskollen.app

import java.sql.DriverManager

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time
import org.scalatest.time.{Seconds, Span}
import se.riksdagskollen.http.{ScalajHttpClient, SyncRunner}
import se.riksdagskollen.db

/**
  * Created by johan on 2016-05-26.
  */
class PersonRepositoryTest extends FunSuite with ScalaFutures {

  implicit val context = scala.concurrent.ExecutionContext.global

  val conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/riks", "root", "")

  test("testing download") {
    val httpClient = new ScalajHttpClient(context)
    val syncer = new SyncRunner(conn, httpClient, context)

    val vote = Vote("yes", "regards", "C049B1D7-6354-11D7-AEB4-0004755030A2", "0584386800218")
    val repo = new db.VoteRepository(conn, 1)
    repo.insert(vote)

    /*
    val s = syncer.run() map { res =>
      println(res)
    }
    */

    //s.isReadyWithin(Span(1000, Seconds))
  }

}
