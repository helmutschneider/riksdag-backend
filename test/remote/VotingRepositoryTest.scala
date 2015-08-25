package remote

import http.{MockResponse, MockClient}
import org.scalatest.time.{Seconds, Span, Millis}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.concurrent.ScalaFutures

import scala.io.Source

/**
 * Created by Johan on 2015-08-24.
 */
class VotingRepositoryTest extends FunSuite with BeforeAndAfter with ScalaFutures {

  var client: MockClient = null
  var repo: VotingRepository = null

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  before {
    client = new MockClient
    repo = new VotingRepository(client)
  }

  test("fetches voting ids as expected") {
    val path: String = getClass.getResource("voting-list-response.json").getPath
    val res: String = Source.fromFile(path).getLines().mkString
    client.respondWith = new MockResponse(200, res)

    var req = repo.fetchVotingIds()

    assert(req.isReadyWithin(Span(2, Seconds)))

    whenReady(req) { res =>
      assert(res(0) == "019135E3-6F23-4136-9252-3877406FB389")
      assert(res(1) == "01ECE25E-9C1E-4286-A442-3C14827A2B72")
    }

  }

}
