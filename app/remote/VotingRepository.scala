package remote

import http.{Request, HttpClientTrait}
import play.api.libs.json.JsArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-25.
 */
class VotingRepository(client: HttpClientTrait) {

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

  def fetchVotingIds(): Future[Seq[String]] = {
    val r = Request(
      "http://data.riksdagen.se/voteringlista/",
      "GET",
      "",
      List(
        "rm" -> "2014/15",
        "sz" -> 1000000.toString,
        "utformat" -> "json",
        "gruppering" -> "votering_id"
      )
    )

    client.send(r) map (res => {
      (res.json \ "voteringlista" \ "votering" \\ "votering_id").map(_.as[String])
    })

  }

  def fetch(): Future[(Seq[Voting], Seq[Vote])] = {
    client.send(req).map(p => {
      val js = p.json \ "voteringlista" \ "votering"
      val duplicatedVotes = js.as[List[Voting]]
      val voteCast = js.as[List[Vote]]

      (duplicatedVotes.toSet.toList, voteCast)
    })
  }

}
