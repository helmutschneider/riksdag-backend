package remote

import http.{Request, HttpClientTrait}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by Johan on 2015-08-25.
 */
class VotingRepository(client: HttpClientTrait)(implicit ec: ExecutionContext) {

  implicit val voteReader = remote.Voting.jsonReader
  implicit val voteCastReader = remote.Vote.jsonReader
  implicit val documentReader = remote.Document.jsonReader

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
      (res.json \ "voteringlista" \ "votering" \\ "votering_id") map (_.as[String])
    })

  }

  def fetchById(id: String): Future[(Voting, Seq[Vote], Document)] = {

    val req = Request(
      s"http://data.riksdagen.se/votering/${id}/json",
      "GET",
      "",
      List()
    )

    client.send(req) map(p => {
      val js = p.json \ "votering" \ "dokvotering" \ "votering"
      val votings = js.as[List[Voting]]
      val voting = votings.head
      val votes = js.as[List[Vote]]
      val doc = (p.json \ "votering" \ "dokument").as[Document]

      (voting, votes, doc)
    })
  }

}
