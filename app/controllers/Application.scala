package controllers

import http.HttpClient
import play.api.mvc._
import remote.SyncManager
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def get = Action.async {
    val client = new HttpClient
    val mgr = new SyncManager(client)
    mgr.run().map(a => Ok(a.toString))
  }

}
