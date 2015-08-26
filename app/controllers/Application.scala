package controllers

import http.HttpClient
import play.api.db.DB
import play.api.libs.json.{Json, JsObject}
import play.api.mvc._
import remote.SyncManager
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

class Application extends Controller {

  def get = Action.async {
    val client = new HttpClient
    val mgr = new SyncManager(client)
    mgr.run().map(a => Ok(a.toString))
  }

  def absentTop = Action {

    val sql = """select
    (p.first_name || ' ' || p.last_name) as name,
    p.party,
    t1.absent,
    (select count(*) from voting v where v.sync_id = p.sync_id) total_votings
    from person p
    inner join (
      select
        p.person_id,
        sum(if(v.result = 2, 1, 0)) absent
      from person p
      inner join vote v
        on v.person_id = p.person_id
      where p.status = 0
      and p.sync_id = (select max(sync_id) from sync)
      group by p.person_id
    ) t1 on p.person_id = t1.person_id
    order by t1.absent desc
    limit 20"""

    DB.withConnection() { conn =>
      val stmt = conn.createStatement()
      val result = stmt.executeQuery(sql)

      var rows = ListBuffer[JsObject]()

      while ( result.next() ) {
        rows += Json.obj(
          "name" -> result.getString("name"),
          "party" -> result.getString("party"),
          "absent" -> result.getInt("absent"),
          "total_votings" -> result.getInt("total_votings")
        )
      }

      val js = Json.toJson(rows)

      Ok(Json.prettyPrint(js))
    }


  }

}
