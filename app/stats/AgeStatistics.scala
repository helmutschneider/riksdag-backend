package stats

import java.sql.Connection
import scala.collection.mutable

import db.{QueryLibrary, Query}
import db.Query.{Sort, Where}

/**
 * Created by Johan on 2015-08-28.
 */
class AgeStatistics(db: Connection) {

  def getGlobalBirthYearDistribution(): List[BirthYearDistribution] = {

    val q = (new Query)
      .select("birth_year", "count(birth_year) as qty")
      .from("person p")
      .groupBy("birth_year")
      .where("p.status = ?", Where.And)
      .where("p.sync_id", QueryLibrary.latestSyncId, Where.And)
      .orderBy("birth_year", Sort.Ascending)

    val stmt = db.prepareStatement(q.sql)
    stmt.setInt(1, remote.Status.Active.id)

    val result = stmt.executeQuery()

    val builder = List.newBuilder[BirthYearDistribution]

    while ( result.next() ) {
      builder += BirthYearDistribution(result.getInt("birth_year"), result.getInt("qty"))
    }

    builder.result
  }

  def getBirthYearDistributionByParty(): Map[String, List[BirthYearDistribution]] = {

    val q = (new Query)
      .select("party", "birth_year", "count(birth_year) as qty")
      .from("person p")
      .groupBy("birth_year", "party")
      .where("p.status = ?", Where.And)
      .where("p.sync_id", QueryLibrary.latestSyncId, Where.And)
      .orderBy("birth_year", Sort.Ascending)
      .orderBy("party", Sort.Ascending)

    val stmt = db.prepareStatement(q.sql)
    stmt.setInt(1, remote.Status.Active.id)

    val result = stmt.executeQuery()

    val resultMap = mutable.Map[String, List[BirthYearDistribution]]()

    while ( result.next() ) {

      val party = result.getString("party")
      val birthYear = result.getInt("birth_year")
      val count = result.getInt("qty")

      resultMap.get(party) match {
        case None => resultMap += party -> List()
        case Some(_) =>
      }
      resultMap.update(party, resultMap(party) :+ BirthYearDistribution(birthYear, count))

    }

    resultMap.toMap
  }

}
