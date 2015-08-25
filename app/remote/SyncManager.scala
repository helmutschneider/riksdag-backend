package remote

import java.sql.Timestamp
import db._
import http.HttpClientTrait
import util.FutureQueue
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Promise, Future}
import org.squeryl.PrimitiveTypeMode._

/**
 * Created by Johan on 2015-08-24.
 */
class SyncManager(httpClient: HttpClientTrait)(implicit ec: ExecutionContext) {

  private def start(): Sync = {
    val startedAt = new Timestamp((new java.util.Date).getTime)
    val s = new Sync(startedAt, None)

    inTransaction {
      db.Schema.syncs.insert(s)
    }

    s
  }

  private def complete(s: Sync): Sync = {
    val completedAt = new Timestamp((new java.util.Date).getTime)
    s.completedAt = Some(completedAt)

    inTransaction {
      db.Schema.syncs.update(s)
    }

    s
  }

  private def syncPeople(s: Sync): Future[Seq[db.Person]] = {
    val repo = new PersonRepository(httpClient)

    repo.fetch() map (people => {

      val rows = people filter (p => {
        (p.location, p.party) match {
          case (Some(x), Some(y)) => true
          case _ => false
        }
      }) map (p => p.toDbPerson(s.id))

      inTransaction {
        // cant batch insert since the primary key wont get injected
        rows foreach (p => db.Schema.people.insert(p))
      }

      rows
    })
  }

  private def syncVotes(s: Sync, people: Seq[db.Person]): Future[Seq[db.Voting]] = {
    val peopleMap = people
      .groupBy(p => p.remoteId)
      .map(kv => (kv._1, kv._2.head.id))

    val repo = new VotingRepository(httpClient)

    val makeLoaderFunc = (id: String) => {
      () => {
        println(s"Loading voting id ${id}")
        repo.fetchById(id) map (kv => {
          val voting = new db.Voting(kv._1.remoteId, new Timestamp(kv._1.date.getTime), s.id)

          inTransaction {
            db.Schema.votings.insert(voting)

            val voteRows = new ListBuffer[db.Vote]()

            kv._2.foreach(v => {
              val personId = peopleMap.get(v.remotePersonId)

              personId match {
                case Some(x) => voteRows += new db.Vote(x, voting.id, v.result)
                case _ => None
              }

            })

            db.Schema.votes.insert(voteRows.toList)
            db.Schema.documents.insert(kv._3.toDbDocument(voting.id))
          }

          println(s"Done loading voting id ${id}")

          voting
        })
      }
    }

    val prom = Promise[Seq[db.Voting]]()

    repo.fetchVotingIds() map (ids => {

      val funcs = ids map (id => makeLoaderFunc(id))
      val queue = new FutureQueue[db.Voting](funcs, 10)

      queue.run() map (res => prom.success(res))
    })

    prom.future

  }

  def run(): Future[Sync] = {

    db.Session.start()
    println("Starting sync")

    val s = this.start()

    println(s.id)

    val result = for {
      people <- this.syncPeople(s)
      votes <- this.syncVotes(s, people)
    } yield (people)

    result map (p => {
      println("Sync complete")
      this.complete(s)
      s
    })

  }

}
