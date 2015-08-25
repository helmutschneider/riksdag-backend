package db

import org.squeryl.SessionFactory
import org.squeryl.adapters.MySQLAdapter
import play.api.db.DB
import play.api.Play.current

/**
 * Created by Johan on 2015-08-22.
 */
object Session {

  def start(): Unit = {
    Class.forName("com.mysql.jdbc.Driver")

    SessionFactory.concreteFactory = Some(() => {
      org.squeryl.Session.create(DB.getConnection(), new MySQLAdapter)
    })
  }

}
