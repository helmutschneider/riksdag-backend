package stats

import java.sql.Connection

import remote.Gender
import remote.Status
import scala.collection.mutable

/**
 * Created by Johan on 2015-08-26.
 */
class GenderStatistics(db: Connection) {

  def getGenderDistrubion(): GenderDistribution = {
    val sql = """
           select
            sum(if(p.gender = ?, 1, 0)) female,
            sum(if(p.gender = ?, 1, 0)) male
           from person p
           where p.sync_id = (
             select max(sync_id)
               from sync
               where completed_at is not null
           )
           and p.status = ?"""

    val stmt = db.prepareStatement(sql)
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
    val sql = """
            select
              p.party,
              sum(if(p.gender = ?, 1, 0)) female,
              sum(if(p.gender = ?, 1, 0)) male
            from person p
            where p.sync_id = (
              select max(sync_id)
                from sync
                where completed_at is not null
            )
            and p.status = ?
            group by p.party"""

    val stmt = db.prepareStatement(sql)
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
