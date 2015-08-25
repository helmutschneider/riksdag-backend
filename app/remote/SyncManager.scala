package remote

import java.sql.Timestamp
import db._
import http.{HttpClientTrait}
import util.FutureQueue
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Promise, Future}
import org.squeryl.PrimitiveTypeMode._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class SyncManager(httpClient: HttpClientTrait) {

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

  private def syncDocuments(s: Sync): Future[Seq[db.Document]] = {
    val repo = new DocumentRepository(httpClient)
    val queue = new FutureQueue[Seq[db.Document]](10)

    // factory function to create an awaitable function on a specific page
    val makeFunction = (page: Int) => {
      () => {
        println(s"Loading page ${page}")
        repo.currentPage = page
        repo.fetch() map (people => {

          val rows = people map (p => p.toDbDocument(s.id))

          inTransaction {
            db.Schema.documents.insert(rows)
          }

          println(s"Page ${page} done")

          rows
        })
      }
    }

    val prom = Promise[Seq[db.Document]]()

    // determine the amount of pages
    repo.getPageCount() map (count => {

      // create functions for each page
      for ( i <- 1 to count ) {
        queue.push(makeFunction(i))
      }

      // run the queue and resolve the promise when it's complete
      queue.run() map (docs => prom.success(docs.flatten))
    })

    prom.future
  }

  private def syncVotes(s: Sync, people: Seq[db.Person]): Future[Seq[db.Voting]] = {
    val peopleMap = people
      .groupBy(p => p.remoteId)
      .map(kv => (kv._1, kv._2.head.id))

    val repo = new VotingRepository(httpClient)
    val queue = new FutureQueue[db.Voting](10)

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

      ids map (id => queue.push(makeLoaderFunc(id)))

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
