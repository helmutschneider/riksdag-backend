package http

import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.ws.InMemoryBody
import play.api.test._
import play.api.test.Helpers._

/**
 * Created by Johan on 2015-08-24.
 */
class HttpClientTest extends FunSuite with BeforeAndAfter {

  var client: HttpClient = null

  before {
    this.client = new HttpClient
  }

  test("Converts RequestTrait to WSRequest correctly") {
    val body = "Hello World"
    val req = new Request("http://google.com", "POST", body, List("p1" -> "1", "p2" -> "2"))

    running(FakeApplication()) {
      val wsr = this.client.toWSRequest(req)
      assert(wsr.url == "http://google.com")
      assert(wsr.method == "POST")

      val bytes = wsr.body.asInstanceOf[InMemoryBody].bytes

      for ( i <- bytes.indices ) {
        assert(bytes(i) == body.getBytes()(i))
      }

      assert(wsr.queryString.get("p1").contains(List("1")))
      assert(wsr.queryString.get("p2").contains(List("2")))

    }

  }

}
