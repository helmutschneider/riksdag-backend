package controllers

import play.api.mvc._
import remote.SyncManager

class Application extends Controller {

  def get = Action {
    val mgr = new SyncManager
    val p = mgr.run()

    Ok("Sync started")
  }

}
