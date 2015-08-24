package remote

import java.sql.Timestamp
import db._
import http.HttpClient
import scala.concurrent.{Future}
import org.squeryl.PrimitiveTypeMode._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class SyncManager {

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
    val client = new HttpClient()
    val repo = new PersonRepository(client)

    repo.fetch().map(people => {

      val rows = people.map(p => p.toDbPerson(s.id))

      inTransaction {
        db.Schema.people.insert(rows)
      }

      rows
    })
  }

  def run(): Future[Sync] = {

    db.Session.start()
    println("Hello World")

    transaction {
      val s = this.start()
      this.syncPeople(s).map(p => {
        println(p.toString())
        this.complete(s)
        s
      })
    }

  }

}
