package se.riksdagskollen.http

import java.sql.{Connection, Timestamp}

import se.riksdagskollen.app._
import se.riksdagskollen.db

import scala.concurrent.{ExecutionContext, Future}

class SyncRunner(connection: Connection, httpClient: HttpClientTrait, context: ExecutionContext) {

  val concurrency: Int = 4

  private def syncPeople(syncId: Int): Future[Seq[Person]] = {
    val personHttpRepo = new PersonRepository(httpClient, context)
    val personDbRepo = new db.PersonRepository(connection)
    implicit val ec = context
    personHttpRepo.fetch() map { res =>
      res filter { p => p.party != null && p.status != null }
    } map { people =>
      people map { person =>
        personDbRepo.insert(person, syncId)
        person
      }
    }
  }

  private def syncVotings(syncId: Int): Future[Seq[(Voting, Seq[Vote])]] = {
    val votingHttpRepo = new VotingRepository(httpClient, context)
    val votingDbRepo = new db.VotingRepository(connection)
    val voteDbRepo = new db.VoteRepository(connection)
    implicit val ec = context
    votingHttpRepo.fetchVotingIds() map { ids =>
      var idx = 0
      val funcs = ids map { id =>
        idx += 1
        val idx2 = idx
        () => {
          println(s"$idx2/${ids.length} fetching $id")
          val fut = votingHttpRepo.fetch(id) map { res =>
            votingDbRepo.insert(res._1, syncId)
            res._2 foreach { voteDbRepo.insert(_, syncId) }
            res
          }

          fut onFailure {
            case e => println(e)
          }

          fut
        }
      }
      val queue = new FutureQueue[(Voting, Seq[Vote])](funcs, concurrency, context)
      queue.run().all()
    } flatMap { res => res }
  }

  def run(): (Sync, Future[Sync]) = {

    val syncDbRepo = new db.SyncRepository(connection)

    implicit val ec = context

    val sync = Sync(new Timestamp((new java.util.Date).getTime))
    val syncId = syncDbRepo.insert(sync)

    val updated = (for {
      people <- syncPeople(syncId)
      votings <- syncVotings(syncId)
    } yield sync) map { s =>
      val updated = s.withCompletedAt(new Timestamp((new java.util.Date).getTime))
      syncDbRepo.update(updated, syncId)
      updated
    }

    (sync, updated)
  }

}
