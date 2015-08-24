package remote

import http.{Request, HttpClientTrait}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class DocumentRepository(client: HttpClientTrait) extends RepositoryTrait[Document] {

  override def fetch(): Future[Seq[Document]] = {
    val req = new Request("http://data.riksdagen.se/personlista/", "GET", "", List("utformat" -> "json"))

    implicit val reader = remote.Document.jsonReader

    client.send(req).map(res => (res.json \ "dokumentlista" \ "dokument").as[List[Document]])
  }

}
