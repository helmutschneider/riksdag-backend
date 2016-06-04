package se.riksdagskollen.http

import java.sql.Timestamp

import org.json4s.{CustomSerializer, DefaultFormats, Formats, JField, JObject, JString, JValue}
import se.riksdagskollen.app.{Vote, Voting}

import scala.concurrent.{ExecutionContext, Future}

class VotingRepository(httpClient: HttpClientTrait, context: ExecutionContext) {

  implicit val formats: Formats = DefaultFormats ++ Seq(
    VotingRepository.voteSerializer
  )

  val listRequest = Request(
    "GET",
    "https://data.riksdagen.se/voteringlista/",
    Seq(
      "Accept" -> "application/json; charset=utf-8",
      "Content-Type" -> "application/json; charset=utf-8"
    ),
    Seq(
      "utformat" -> "json",
      "gruppering" -> "votering_id",
      "sz" -> "100"
    )
  )

  val votingRequest = (id: String) => Request(
    "GET",
    s"https://data.riksdagen.se/votering/$id/json",
    Seq(
      "Accept" -> "application/json; charset=utf-8",
      "Content-Type" -> "application/json; charset=utf-8"
    )
  )

  def fetchVotingIds(): Future[Seq[String]] = {
    implicit val ec = context
    httpClient.send(listRequest) map { res =>
      (res.json \ "voteringlista" \ "votering" \ "votering_id").extract[Seq[String]]
    }
  }

  def fetch(id: String): Future[(Voting, Seq[Vote])] = {
    implicit val ec = context
    httpClient.send(votingRequest(id)) map { res =>
      val date = Timestamp.valueOf((res.json \ "votering" \ "dokument" \ "datum").extract[String])
      val voting = Voting(id, date)
      val votes = (res.json \ "votering" \ "dokvotering" \ "votering").extract[Seq[Vote]]
      (voting, votes)
    }
  }

}

object VotingRepository {

  private implicit val defaultFormats: Formats = DefaultFormats

  val voteSerializer = new CustomSerializer[Vote](formats => (
    {
      case x: JValue =>
        Vote(
          (x \ "rost").extract[String],
          (x \ "avser").extract[String],
          (x \ "votering_id").extract[String],
          (x \ "intressent_id").extract[String]
        )
    },
    {
      case x: Vote =>
        JObject(
          JField("value", JString(x.value)),
          JField("regarding", JString(x.regarding))
        )
    }
    ))

}