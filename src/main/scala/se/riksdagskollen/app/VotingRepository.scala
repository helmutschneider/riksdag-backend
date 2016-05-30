package se.riksdagskollen.app

import org.json4s.{CustomSerializer, DefaultFormats, Formats, JField, JObject, JString, JValue}
import se.riksdagskollen.http.{HttpClientTrait, Request}

import scala.concurrent.{ExecutionContext, Future}
import java.util.Date

/**
  * Created by johan on 2016-05-29.
  */
class VotingRepository(httpClient: HttpClientTrait, context: ExecutionContext) {

  implicit val formats: Formats = DefaultFormats + VotingRepository.votingSerializer

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
      "sz" -> "10000"
    )
  )

  val votingRequest = (id: String) => Request(
    "GET",
    s"https://data.riksdagen.se/voting/$id/json",
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

  def fetch(id: String): Future[Voting] = {
    implicit val ec = context
    httpClient.send(votingRequest(id)) map { res =>
      val json = res.json \ "votering" \ "dokvotering" \ "votering"
      val votings = json.extract[Seq[Voting]]
      votings.head
    }
  }

}

object VotingRepository {

  private implicit val defaultFormats: Formats = DefaultFormats

  val votingSerializer = new CustomSerializer[Voting](formats => (
    {
      case x: JValue =>
        Voting(
          (x \ "votering_id").extract[String],
          (x \ "datum").extract[Date]
        )
    },
    {
      case x: Voting =>
        JObject(
          JField("id", JString(x.id)),
          JField("date", JString(x.date.toString))
        )
    }
    ))

  val voteSerializer = new CustomSerializer[Vote](formats => (
    {
      case x: JValue =>
        Vote(
          (x \ "rost").extract[String],
          (x \ "avser").extract[String]
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