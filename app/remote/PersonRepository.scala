package remote

import http.HttpClientTrait
import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class PersonRepository(client: HttpClientTrait) extends RepositoryTrait[Person] {

  def fetch(): Future[Seq[Person]] = {

    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withMethod("GET")
      .withQueryString(
        "utformat" -> "json"
      )

    implicit val reader = remote.Person.jsonReader

    client.send(request).map(res => {
      (res.json \ "personlista" \ "person").as[List[remote.Person]]
    })

  }

}
