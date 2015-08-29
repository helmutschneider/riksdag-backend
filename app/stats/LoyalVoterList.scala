package stats

import java.sql.Connection

import db.QueryLibrary
import play.api.libs.json.{Json, JsValue, Writes}
import remote.Result
import remote.Result.Result
import remote.Result.Result

import scala.collection.mutable


case class LoyalVoter(firstName: String, lastName: String, party: String, loyalVote: Result, majority: Result, vote: Result)

object LoyalVoter {
  val jsonWriter = new Writes[LoyalVoter] {
    override def writes(o: LoyalVoter): JsValue = Json.obj(
      "first_name" -> o.firstName,
      "last_name" -> o.lastName,
      "party" -> o.party,
      "loyal_vote" -> o.loyalVote,
      "majority" -> o.majority,
      "vote" -> o.vote
    )
  }
}

/**
 * Created by Jacob on 2015-08-28.
 */
class LoyalVoterList(db: Connection) {



  def getLoyalVoterList(): List[LoyalVoter] ={

    val stmt = db.prepareStatement(QueryLibrary.getLoyalVoterList.sql)

    val result = stmt.executeQuery()

    val resultList = mutable.MutableList[LoyalVoter]()

    while(result.next()){
      resultList += LoyalVoter(result.getString("first_name"), result.getString("last_name"), result.getString("party"), Result(result.getInt("loyal_voter")), Result(result.getInt("majority")), Result(result.getInt("result")))
    }

    resultList.toList

  }

}
