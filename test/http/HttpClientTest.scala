package http

import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.ws.InMemoryBody
import play.api.test._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class HttpClientTest extends FunSuite with BeforeAndAfter {

  implicit lazy val app: FakeApplication = FakeApplication(
    additionalConfiguration = Map(
      "db.default.driver" -> "org.h2.Driver",
      "db.default.url" -> "jdbc:h2:mem:play;MODE=MYSQL"
    )
  )

  var client: HttpClient = null

  before {
    this.client = new HttpClient
  }

  test("Converts RequestTrait to WSRequest correctly") {
    val body = "Hello World"
    val req = new Request("http://google.com", "POST", body, List("p1" -> "1", "p2" -> "2"))

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
