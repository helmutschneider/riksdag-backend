package stats

import java.sql.Connection

import db.QueryLibrary
import play.api.libs.json.{Json, JsValue, Writes}
import remote.Result
import remote.Result.Result
import remote.Result.Result

import scala.collection.mutable

case class Loyality(personId: Int, firstName: String, lastName: String, party: String, disloyalCount: Int)

object LoyalVoter {

  val loyalityJsonWriter = new Writes[Loyality] {
    override def writes(o: Loyality): JsValue = Json.obj(
      "person_id" -> o.personId,
      "first_name" -> o.firstName,
      "last_name" -> o.lastName,
      "party" -> o.party,
      "disloyal_count" -> o.disloyalCount
    )
  }
}

/**
 * Created by Jacob on 2015-08-28.
 */
class LoyalityStatistics(db: Connection) {

  def getLoyalityByPerson(): List[Loyality] = {

    val sql =
      """
        select
        	p.person_id,
        	p.first_name,
        	p.last_name,
        	p.party,
        	t6.disloyal_count
        from (
        	/* compute the number of times a person has been disloyal */
        	select
        		t5.person_id,
        		sum(if(t5.is_loyal = 0, 1, 0)) disloyal_count
        	from (
        	    /* compare the result of the vote with the party's most common vote
        	     to determine if the person is loyal */

        		select
        			v.person_id,
        			if(v.result = t4.most_common, 1, 0) as is_loyal
        		from vote v
        		inner join person p
        		on p.person_id = v.person_id
        		inner join (
        			select
        				t1.voting_id,
        				t1.party,
        				t1.result as most_common
        			from (
        				/* compute the quantity votes (no/yes/abstaining)
        				   grouped by voting id and party */
        				select
        					v.voting_id,
        					v.result,
        					p.party,
        					count(v.result) qty
        				from vote v
        				inner join person p
        				on p.person_id = v.person_id

        				/* remove "absent" votes */
        				where v.result != ?
        				group by v.voting_id, p.party, v.result
        			) t1

        			/* join the quantity of the most common vote so we can determine
        			   the most common vote of each party */
        			inner join (

        			    /* compute the quantity of the most common vote
        			       grouped by voting id and party */
        				select
        					voting_id,
        					party,
        					max(qty) most_common_count
        				from (

        				    /* compute the quantity votes (no/yes/abstaining)
        				       grouped by voting id and party */
        					select
        						v.voting_id,
        						p.party,
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
        					group by v.voting_id, p.party, v.result
        				) t2
        				group by t2.voting_id, t2.party
        			) t3
        			on t3.voting_id = t1.voting_id
        			and t3.most_common_count = t1.qty
        			and t3.party = t1.party
        		) t4
        		on t4.voting_id = v.voting_id
        		and t4.party = p.party
        		/* remove "absent" votes */
        		where v.result != ?
        	) t5
        	group by t5.person_id
        ) t6
        inner join person p
        on p.person_id = t6.person_id
        order by t6.disloyal_count desc
      """

    val stmt = db.prepareStatement(sql)

    for ( i <- 1 to 3 ) {
      stmt.setInt(i, remote.Result.Absent.id)
    }

    val result = stmt.executeQuery()

    val builder = List.newBuilder[Loyality]
    while ( result.next() ) {
      builder += Loyality(
        result.getInt("person_id"),
        result.getString("first_name"),
        result.getString("last_name"),
        result.getString("party"),
        result.getInt("disloyal_count")
      )
    }

    builder.result()
  }

}
