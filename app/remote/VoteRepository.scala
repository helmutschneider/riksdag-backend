package remote

import http.{Request, HttpClientTrait}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-25.
 */
class VoteRepository(client: HttpClientTrait) {

  implicit val voteReader = remote.Voting.jsonReader
  implicit val voteCastReader = remote.Vote.jsonReader

  val req = Request(
    "http://data.riksdagen.se/voteringlista/",
    "GET",
    "",
    List(
      "rm" -> "2014/15",
      "sz" -> 1000000.toString,
      "utformat" -> "json"
    )
  )

  def fetch(): Future[(Seq[Voting], Seq[Vote])] = {
    client.send(req).map(p => {
      val js = p.json \ "voteringlista" \ "votering"
      val duplicatedVotes = js.as[List[Voting]]
      val voteCast = js.as[List[Vote]]

      (duplicatedVotes.toSet.toList, voteCast)
    })
  }

}
