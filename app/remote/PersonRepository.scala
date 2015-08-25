package remote

import http.{Request, HttpClientTrait}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class PersonRepository(client: HttpClientTrait) extends RepositoryTrait[Person] {

  override def fetch(): Future[Seq[Person]] = {

    val req = new Request("http://data.riksdagen.se/personlista/", "GET", "", List("utformat" -> "json"))

    implicit val reader = remote.Person.jsonReader

    client.send(req).map(res => {
      (res.json \ "personlista" \ "person").as[List[remote.Person]]
    })

  }

}