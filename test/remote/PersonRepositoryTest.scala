package remote

import http.{MockResponse, MockClient}
import org.scalatest.time.{Seconds, Span, Millis}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext.Implicits.global

import scala.io.Source

/**
 * Created by Johan on 2015-08-24.
 */
class PersonRepositoryTest extends FunSuite with BeforeAndAfter with ScalaFutures {

  var client: MockClient = null
  var repo: PersonRepository = null

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  before {
    client = new MockClient
    repo = new PersonRepository(client)
  }

  test("fetches people as expected") {
    val path: String = getClass.getResource("person-response.json").getPath
    val res: String = Source.fromFile(path).getLines().mkString

    println(res)

    client.respondWith = new MockResponse(200, res)

    val req = repo.fetch()

    assert(req.isReadyWithin(Span(2, Seconds)))

    whenReady(req) { result =>
      assert(result.head.remoteId == "0363228965800")
    }

  }

}
