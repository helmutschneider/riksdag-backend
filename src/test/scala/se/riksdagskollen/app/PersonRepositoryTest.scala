package se.riksdagskollen.app

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time
import se.riksdagskollen.http.ScalajHttpClient

/**
  * Created by johan on 2016-05-26.
  */
class PersonRepositoryTest extends FunSuite with ScalaFutures {

  val context = scala.concurrent.ExecutionContext.global

  test("testing download") {
    val httpClient = new ScalajHttpClient(context)
    val repo = new PersonRepository(httpClient, context)

    val p = repo.fetch()
    p.isReadyWithin(time.Span(5000, time.Milliseconds))
  }

}
