package remote

import java.sql.Timestamp
import db._
import http.HttpClient
import play.api.libs.ws.WS
import scala.concurrent.{ExecutionContext, Future}
import org.squeryl.PrimitiveTypeMode._
import play.api.Play.current

/**
 * Created by Johan on 2015-08-24.
 */
class SyncManager(context: ExecutionContext) {

  private implicit val ctx: ExecutionContext = context

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

  private def syncPeople(s: Sync): Future[List[db.Person]] = {
    val client = new HttpClient()
    val request = WS.url("http://data.riksdagen.se/personlista/")
      .withMethod("GET")
      .withQueryString(
        "utformat" -> "json"
      )

    implicit val reader = remote.Person.jsonReader

    client.send(request).map(res => {
      (res.json \ "personlista" \ "person").as[List[remote.Person]]
    }).map(people => {

      val rows = people.map(p => new db.Person(
        p.remoteId,
        p.birthYear,
        p.gender,
        p.firstName,
        p.lastName,
        p.party,
        p.location,
        p.imageUrl,
        s.id))

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
