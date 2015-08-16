package db

/**
 * Created by Johan on 2015-08-16.
 */
import sorm._
import remote.Person
import app.Environment

object Db extends Instance(
  entities = Set() + Entity[Person](),
  url = s"jdbc:mysql://${Environment.getOrElse("DB_HOST", "no-host")}/${Environment.getOrElse("DB_NAME", "no-db-name")}",
  user = Environment.getOrElse("DB_USER", "no-user"),
  password = Environment.getOrElse("DB_PASSWORD", "no-pwd"),
  initMode = InitMode.DoNothing,
  poolSize = 1
)
