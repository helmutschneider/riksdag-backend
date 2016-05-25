package se.riksdagskollen.app

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import se.riksdagskollen.http.{HttpClientTrait, Request}

abstract class AbstractHttpClientTest extends FunSuite with BeforeAndAfter with ScalaFutures {

  def getHttpClient: HttpClientTrait

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))
  var httpClient: Option[HttpClientTrait] = None

  before {
    httpClient = Some(getHttpClient)
  }

  after {
    httpClient = None
  }

  test("response code should be 200") {
    val req = Request("GET", "http://www.google.com")
    val fut = httpClient.get.send(req)

    whenReady(fut) { res =>
      assert(res.isSuccess)
      assert(res.body != "")
    }
  }

}
