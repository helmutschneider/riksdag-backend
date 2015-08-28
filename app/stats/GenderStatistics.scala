package stats

import java.sql.Connection

import db.Query.Where
import db.{QueryLibrary, Query}
import remote.Gender
import remote.Status
import scala.collection.mutable

/**
 * Created by Johan on 2015-08-26.
 */
class GenderStatistics(db: Connection) {

  def getGlobalGenderDistrubion(): GenderDistribution = {

    val q = (new Query)
      .select(
        "sum(if(p.gender = ?, 1, 0)) female",
        "sum(if(p.gender = ?, 1, 0)) male"
      ).from("person p")
      .where("p.sync_id", QueryLibrary.latestSyncId, Where.And)
      .where("p.status = ?", Where.And)

    val stmt = db.prepareStatement(q.sql)
    stmt.setInt(1, Gender.Female.id)
    stmt.setInt(2, Gender.Male.id)
    stmt.setInt(3, Status.Active.id)

    val result = stmt.executeQuery()

    var count = (0, 0)
    while ( result.next() ) {
      count = (result.getInt("female"), result.getInt("male"))
    }

    (GenderDistribution.apply _).tupled(count)
  }

  def getGenderDistributionByParty(): Map[String, GenderDistribution] = {

    val q = (new Query)
      .select(
        "p.party",
        "sum(if(p.gender = ?, 1, 0)) female",
        "sum(if(p.gender = ?, 1, 0)) male"
      ).from("person p")
      .where("p.sync_id", QueryLibrary.latestSyncId, Where.And)
      .where("p.status = ?", Where.And)
      .groupBy("p.party")

    val stmt = db.prepareStatement(q.sql)
    stmt.setInt(1, Gender.Female.id)
    stmt.setInt(2, Gender.Male.id)
    stmt.setInt(3, Status.Active.id)

    val result = stmt.executeQuery()
    val resultMap = mutable.Map[String, GenderDistribution]()

    while ( result.next() ) {
      resultMap += (result.getString("party") -> GenderDistribution(result.getInt("female"), result.getInt("male")))
    }

    resultMap.toMap
  }

}
