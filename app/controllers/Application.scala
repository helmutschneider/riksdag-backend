package controllers

import play.api.mvc._
import remote.SyncManager
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def get = Action.async {
    val context = ExecutionContext.fromExecutor(global)
    val mgr = new SyncManager(context)
    mgr.run().map(sync => Ok(sync.toString))
  }

}
