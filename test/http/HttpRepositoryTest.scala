package test.http

import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSCookie, WSResponse}

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.xml.Elem
import org.scalatest._
import play.api.libs.functional.syntax._
import org.scalatest.concurrent._
import org.scalatest.time._
import http._
import play.api.test._
import play.api.test.Helpers._

class HttpRepositoryTest extends FunSuite with BeforeAndAfter with ScalaFutures {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

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

    var response: String = ""

    override def send(req: WSRequest): Future[WSResponse] = {
      val res = new MockResponse(this.response)
      val promise = Promise[WSResponse]()

      Future {
        Thread.sleep(1000)
        promise.success(res)
      }

      return promise.future
    }
  }

  case class MockModel(id: Int, name: String) extends Model {
    override def identifier: Any = this.id
  }

  var client: MockClient = null
  var repository: HttpRepository[MockModel] = null

  before {

    implicit val reader: Reads[MockModel] = (
      (JsPath \ "id").read[Int] and
        (JsPath \ "name").read[String]
      )(MockModel.apply _)

    implicit val writer: Writes[MockModel] = new Writes[MockModel] {
      override def writes(o: MockModel): JsValue = Json.obj(
        "id" -> o.id,
        "name" -> o.name
      )
    }

    this.client = new MockClient
    this.repository = new HttpRepository[MockModel](this.client, "http://some-url.com/api/models")
  }

  test("get single item should work properly") {

    this.client.response = """{"id":1,"name":"Helmut"}"""

    running(FakeApplication()) {
      val res = this.repository.get(1)

      assert(res.futureValue == MockModel(1, "Helmut"))
    }

  }

  test("get list of items should work properly") {
    this.client.response = """[{"id":1,"name":"Helmut"},{"id":2,"name":"Pelle"}]"""

    running(FakeApplication()) {
      val res = this.repository.get()

      assert(res.futureValue == List(MockModel(1, "Helmut"), MockModel(2, "Pelle")))
    }

  }

}
