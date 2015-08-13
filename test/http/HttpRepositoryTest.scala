package http

import org.junit.runner.RunWith
import org.junit.{Test, Before}
import play.api.libs.json._
import play.api.libs.ws.{WSCookie, WSResponse}

import scala.concurrent.Future
import scala.xml.Elem

class HttpRepositoryTest {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  class MockResponse(respondWith: String) extends WSResponse {
    override def allHeaders: Map[String, Seq[String]] = ???
    override def statusText: String = ???
    override def underlying[T]: T = ???
    override def xml: Elem = ???
    override def body: String = ???
    override def header(key: String): Option[String] = ???
    override def cookie(name: String): Option[WSCookie] = ???
    override def bodyAsBytes: Array[Byte] = ???
    override def cookies: Seq[WSCookie] = ???
    override def status: Int = ???
    override def json: JsValue = {
      return Json.parse(respondWith)
    }
  }

  class MockClient extends HttpClientTrait {

    var response: String = null
    override def get(url: String): Future[WSResponse] = {
      Future { new MockResponse(response) }
    }
    override def post(url: String, body: String): Future[WSResponse] = ???
    override def put(url: String, body: String): Future[WSResponse] = ???
    override def delete(url: String): Future[WSResponse] = ???
  }

  class MockModel(id: Int, name: String) extends Model {
    override def identifier: Any = this.id
  }

  var client: HttpClientTrait = null
  var repository: HttpRepository[MockModel] = null

  @Before
  def setUp = {
    println("Before")
  }

  @Test
  def testGetSingle = {
    println("Hello World")
  }

}
