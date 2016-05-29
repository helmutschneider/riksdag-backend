package se.riksdagskollen.app

import java.sql.{Connection, Statement, Timestamp}

import se.riksdagskollen.db.Repository
import se.riksdagskollen.http.HttpClientTrait

import scala.concurrent.{ExecutionContext, Future}

class SyncRunner(connection: Connection, httpClient: HttpClientTrait, context: ExecutionContext) {

  def run(): Future[Sync] = {
    val personHttpRepo = new PersonRepository(httpClient, context)
    val personDbRepo = Repository.forPerson(connection)
    val syncDbRepo = Repository.forSync(connection)
    val startedAt = new Timestamp((new java.util.Date).getTime)

    implicit val ec = context

    connection.setAutoCommit(false)

    personHttpRepo.fetch() map { res =>
      res filter { p => p.party != null && p.status != null }
    } map { people =>
      val sync = syncDbRepo.insert(Sync(startedAt, new Timestamp((new java.util.Date).getTime)))
      people map { _.withSyncId(sync.databaseId.get) } map { personDbRepo.insert }
      connection.commit()
      sync
    }

  }


  /*
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
                case Some(x) => voteRows += new db.Vote(x, voting.id, v.result, v.concerns)
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
      val queue = new FutureQueue[db.Voting](funcs, 5)

      queue.run() map (res => prom.success(res))
    })

    prom.future

  }
  */

}
