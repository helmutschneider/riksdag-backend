package remote

import http.{RequestTrait, Request, HttpClientTrait}
import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class DocumentRepository(client: HttpClientTrait) {

  // the api can't handle more than 200 rows in the result
  val perPage = 200
  var currentPage = 1
  implicit val reader = remote.Document.jsonReader

  def makeRequest(page: Int): RequestTrait = {
    new Request("http://data.riksdagen.se/dokumentlista/", "GET", "", List(
      "utformat" -> "json",
      "p" -> page.toString,
      "sz" -> perPage.toString
    ))
  }

  def getPageCount(): Future[Int] = {
    client.send(makeRequest(1)).map(res => {
      (res.json \ "dokumentlista" \ "@sidor").as[String].toInt
    })
  }

  def fetch(): Future[Seq[Document]] = {
    val req = makeRequest(currentPage)
    client.send(req).map(res => {
      (res.json \ "dokumentlista" \ "dokument").as[List[Document]]
    })
  }

}
