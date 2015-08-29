package db

import db.Query.Where

/**
 * Created by johan on 15-08-28.
 */
object QueryLibrary {

  lazy val latestSyncId = (new Query)
    .select("max(sync_id)")
    .from("sync")
    .where("completed_at is not null", Where.And)

  /**
   * Get how each party distributed their votes over every voting
   */
  val getAllVotingResults = (new Query)
    .select("""p.party, v.voting_id,sum(if(v.result = 0, 1, 0)) no,
                               			    sum(if(v.result = 1, 1, 0)) yes,
                               			    sum(if(v.result = 2, 1, 0)) absent,
                               			    sum(if(v.result = 3, 1, 0)) abstaining""")
    .from("person p")
    .join("vote v", "v.person_id = p.person_id", db.Query.Join.Inner)
    .groupBy("v.voting_id, p.party")

  val getPartyMajorityVote = (new Query)
    .select("""t1.party,
			t1.voting_id,
			CASE
		        WHEN t1.no/(t1.no+t1.yes+t1.absent+t1.abstaining) >= 0.5 THEN 0
		        WHEN t1.yes/(t1.no+t1.yes+t1.absent+t1.abstaining) >= 0.5 THEN 1
		        WHEN t1.absent/(t1.no+t1.yes+t1.absent+t1.abstaining) >= 0.5 THEN 2
		        WHEN t1.abstaining/(t1.no+t1.yes+t1.absent+t1.abstaining) >= 0.5 THEN 3
		   END majority""")
    .from(getAllVotingResults, "t1")

  val getLoyalVoterList = (new Query)
    .select("""p.first_name,
		p.last_name,
		p.party,
		if(t2.majority = v.result or v.result = 2, 0, 1) loyal_voter,
    t2.majority,
    v.result""")
    .from(getPartyMajorityVote, "t2")
    .join("person p", "p.party =  t2.party", db.Query.Join.Inner)
    .join("vote v", "p.person_id = v.person_id and t2.voting_id = v.voting_id", db.Query.Join.Inner)

}
