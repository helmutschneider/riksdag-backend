package db

/**
 * Created by Johan on 2015-08-23.
 */

object Schema extends org.squeryl.Schema {

  val people = table[Person]("person")
  val syncs = table[Sync]("sync")
  val documents = table[Document]("document")

}
