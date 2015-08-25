package remote

import java.sql.Timestamp
import db._
import http.{HttpClientTrait, HttpClient}
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

    repo.fetch().map(people => {

      val rows = people.map(p => p.toDbPerson(s.id))

      inTransaction {
        // cant batch insert since the primary key wont get injected
        rows.foreach(p => db.Schema.people.insert(p))
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
        repo.fetch().map(people => {

          val rows = people.map(p => p.toDbDocument(s.id))

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
    repo.getPageCount().map(count => {

      // create functions for each page
      for ( i <- 1 to count ) {
        queue.push(makeFunction(i))
      }

      // run the queue and resolve the promise when it's complete
      queue.run().map(docs => prom.success(docs.flatten))
    })

    prom.future
  }

  private def syncVotes(s: Sync, people: Seq[db.Person]): Future[Seq[db.Voting]] = {
    val peopleMap = people
      .groupBy(p => p.remoteId)
      .map(kv => (kv._1, kv._2.head.id))

    val repo = new VoteRepository(httpClient)

    repo.fetch().map(kv => {
      val votingRows = kv._1.map(v => {
        new db.Voting(v.remoteId, new Timestamp(v.date.getTime), s.id)
      })

      inTransaction {
        // cant batch insert since the primary key wont get injected
        votingRows.foreach(v => db.Schema.votings.insert(v))
      }

      val votingMap = votingRows.groupBy(p => p.remoteId).map(kv => (kv._1, kv._2.head.id))
      val voteRows = new ListBuffer[db.Vote]()

      kv._2.foreach(v => {
        val personId = peopleMap.get(v.remotePersonId)
        val votingId = votingMap.get(v.remoteId)

        (personId, votingId) match {
          case (Some(x), Some(y)) => voteRows += new db.Vote(x, y, v.result)
          case _ => None
        }

      })

      inTransaction {
        db.Schema.votes.insert(voteRows.toList)
      }

      votingRows
    })
  }

  def run(): Future[Sync] = {

    db.Session.start()
    println("Starting sync")

    val s = this.start()

    println(s.id)

    val result = for {
      //documents <- this.syncDocuments(s)
      people <- this.syncPeople(s)
      votes <- this.syncVotes(s, people)
    } yield (people)

    result.map(p => {
      println("Sync complete")
      this.complete(s)
      s
    })

  }

}
