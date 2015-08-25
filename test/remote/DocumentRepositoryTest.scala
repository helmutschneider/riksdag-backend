package remote

import http.{MockClient, MockResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.io.Source

/**
 * Created by Johan on 2015-08-24.
 */
class DocumentRepositoryTest extends FunSuite with BeforeAndAfter with ScalaFutures {

  var client: MockClient = null
  var repo: DocumentRepository = null

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  before {
    client = new MockClient
    repo = new DocumentRepository(client)
  }

  test("fetches documents as expected") {
    val path: String = getClass.getResource("document-response.json").getPath
    val res: String = Source.fromFile(path).getLines().mkString
    client.respondWith = new MockResponse(200, res)

    var req = repo.fetch()

    assert(req.isReadyWithin(Span(2, Seconds)))

    whenReady(req) { result =>
      assert(result(0).remoteId == "49241")
    }

  }

}
