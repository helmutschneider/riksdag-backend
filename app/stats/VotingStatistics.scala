package stats

import java.sql.Connection

import db.QueryLibrary
import play.api.libs.json.{JsObject, Json, JsValue, Writes}
import remote.Result
import remote.Result.Result
import remote.Result.Result

import scala.collection.mutable

case class PersonLoyalty(remoteId: String, firstName: String, lastName: String, party: String, disloyalCount: Int)

case class DisloyalVoting(remoteId: String, title: String, concerns: String, personVoted: Int, partyVoted: Int)

case class Consensus(party: String, consensusTable: Map[String, Float])

object LoyalVoter {

  val loyaltyJsonWriter = new Writes[PersonLoyalty] {
    override def writes(o: PersonLoyalty): JsValue = Json.obj(
      "person_original_id" -> o.remoteId,
      "first_name" -> o.firstName,
      "last_name" -> o.lastName,
      "party" -> o.party,
      "disloyal_count" -> o.disloyalCount
    )
  }

  val disloyalVotingJsonWriter = new Writes[DisloyalVoting] {
    override def writes(o: DisloyalVoting): JsValue = Json.obj(
      "voting_original_id" -> o.remoteId,
      "title" -> o.title,
      "concerns" -> o.concerns,
      "person_voted" -> o.personVoted,
      "party_voted" -> o.partyVoted
    )
  }

  val consensusWriter = new Writes[Seq[Consensus]] {
    override def writes(o: Seq[Consensus]): JsValue = {
      val consMap = o.map { v =>
        v.party -> v.consensusTable
      }.toMap

      Json.toJson(consMap)
    }
  }

}

/**
 * Created by Jacob on 2015-08-28.
 */
class VotingStatistics(db: Connection) {

  val votesByResult = """
         select
            v.voting_id,
            p.party,
            v.concerns,
            v.result,
            count(*) qty
         from vote v
         inner join person p
            on p.person_id = v.person_id
         /* remove "absent" votes */
         where v.result != ?
         group by v.voting_id, p.party, v.concerns, v.result
  """

  val votesForMostCommon = """
        select
                voting_id,
                party,
                concerns,
                max(qty) most_common_count
        from (
            /* compute the quantity votes (no/yes/abstaining)
               grouped by voting id and party */
                select
                    v.voting_id,
                    p.party,
                    v.concerns,
                    count(*) qty
                from vote v
                inner join person p
                    on p.person_id = v.person_id
                inner join voting v2
                    on v2.voting_id = v.voting_id
                and v2.sync_id = (
                    select max(sync_id)
                    from sync
                    where completed_at is not null
                )
                /* remove "absent" votes */
                where v.result != ?
                group by v.voting_id, p.party, v.concerns, v.result
        ) t2
        group by t2.voting_id, t2.party, t2.concerns
  """

  val mostCommonVote = s"""
          select
              t1.voting_id,
              t1.party,
              t1.concerns,
              t1.result as most_common
          from ( $votesByResult ) t1
          /* join the quantity of the most common vote so we can determine
              the most common vote of each party */
          inner join ( $votesForMostCommon ) t3
          on t3.voting_id = t1.voting_id
          and t3.most_common_count = t1.qty
          and t3.party = t1.party
          and t3.concerns = t1.concerns
  """

  def getDisloyalVotings(personRemoteId: String): List[DisloyalVoting] = {

    val sql = s"""
                select
                    v1.remote_id,
                    d.title,
                    v.concerns,
                    v.result as person_voted,
                    t4.most_common as party_voted
                from vote v
                inner join voting v1
                    on v.voting_id = v1.voting_id
                inner join person p
                    on p.person_id = v.person_id
                and p.remote_id = ?
                inner join document d
                    on d.voting_id = v.voting_id
                inner join ( $mostCommonVote ) t4
                    on t4.voting_id = v.voting_id
                    and t4.party = p.party
                    and t4.concerns = v.concerns
                /* remove "absent" votes */
                where v.result != ?
                and v.result != t4.most_common
  """

    val stmt = db.prepareStatement(sql)
    stmt.setString(1, personRemoteId)

    for ( i <- 2 to 4 ) {
      stmt.setInt(i, remote.Result.Absent.id)
    }

    val result = stmt.executeQuery()

    val builder = List.newBuilder[DisloyalVoting]

    while ( result.next() ) {
      builder += DisloyalVoting(
        result.getString("remote_id"),
        result.getString("title"),
        result.getString("concerns"),
        result.getInt("person_voted"),
        result.getInt("party_voted")
      )
    }

    builder.result()
  }

  def getLoyalty(): List[PersonLoyalty] = {

    val sql = s"""
        select
            p.remote_id,
              p.first_name,
              p.last_name,
              p.party,
              t5.disloyal_count
          from person p
          inner join (
                select
                    p.person_id,
                    sum(if(v.result != t4.most_common, 1, 0)) as disloyal_count
                from person p
                inner join vote v
                    on v.person_id = p.person_id
                inner join ( $mostCommonVote ) t4
                    on t4.voting_id = v.voting_id
                    and t4.concerns = v.concerns
                    and t4.party = p.party
                where v.result != ?
                group by p.person_id
          ) t5
          on p.person_id = t5.person_id
          order by t5.disloyal_count desc
      """

    val stmt = db.prepareStatement(sql)

    for ( i <- 1 to 3 ) {
      stmt.setInt(i, remote.Result.Absent.id)
    }

    val result = stmt.executeQuery()

    val builder = List.newBuilder[PersonLoyalty]
    while ( result.next() ) {
      builder += PersonLoyalty(
        result.getString("remote_id"),
        result.getString("first_name"),
        result.getString("last_name"),
        result.getString("party"),
        result.getInt("disloyal_count")
      )
    }

    builder.result()
  }

  def getVotingConsensus(): Seq[Consensus] = {

    val sql = s"""
    select
        party1,
        party2,
        agrees_count/t7.total_votes as consensus_percentage
    from (
        select
            t4.party as party1,
            t5.party as party2,
            sum((t4.most_common = t5.most_common)) as agrees_count
        from ( $mostCommonVote ) t4
        inner join ( $mostCommonVote ) t5
          on t4.voting_id = t5.voting_id
          and t4.concerns = t5.concerns
          and t4.party != t5.party
        group by t4.party, t5.party
    ) t6, (
        select
            count(*) as total_votes
        from (
            select v.voting_id
            from voting v
            inner join vote v1
                on v1.voting_id = v.voting_id
            where v.sync_id = (
                select max(sync_id)
                from sync
                where completed_at is not null
            )
            group by v.voting_id, v1.concerns
        ) t10
    ) t7
  """

    val stmt = db.prepareStatement(sql)

    for ( i <- 1 to 4 ) {
      stmt.setInt(i, remote.Result.Absent.id)
    }

    val result = stmt.executeQuery()

    val partyMap = collection.mutable.Map[String, collection.mutable.Map[String, Float]]()
    while ( result.next() ) {

      val party = result.getString("party1")
      val party2 = result.getString("party2")

      if ( !partyMap.contains(party) ) {
        partyMap += party -> collection.mutable.Map[String, Float]()
      }

      partyMap(party) += (party2 -> result.getFloat("consensus_percentage"))
    }

    partyMap.map(kv => Consensus(kv._1, kv._2.toMap)).toList
  }

}
