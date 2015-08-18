package db

import remote.Person
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-18.
 */
class PersonRepository(db: Database, query: TableQuery[PersonTable]) extends Repository[Person] {

  private val selectAction = this.query.map(p => {
    (p.id, p.personId, p.birthYear, p.gender, p.firstName, p.lastName, p.party, p.location, p.imageUrl)
  })

  private val insertAction = this.query.map(p => {
    (p.personId, p.birthYear, p.gender, p.firstName, p.lastName, p.party, p.location, p.imageUrl)
  })

  override def findAll(): Future[Seq[Person]] = {
    val result = this.db.run(selectAction.result)

    // execute query and map every row to a person object
    result.map(p => {
      p.map(f => {
        val person = Person(f._2, f._3, f._4, f._5, f._6, f._7, f._8, f._9)
        person.id = Option[Int](f._1)
        person
      })
    })

  }

  override def save(item: Person): Future[Boolean] = {
    this.save(List(item))
  }

  override def save(items: Seq[Person]): Future[Boolean] = {

    val data = items.map(item => {
      (item.personId, item.birthYear, item.gender, item.firstName, item.lastName, item.party, item.location, item.imageUrl)
    })

    val result = insertAction ++= data

    this.db.run(result).map {
      case Some(id) => id != 0
      case None => false
    }
  }

  override def findOne(id: Int): Future[Option[Person]] = {

    val action = selectAction.filter(p => p._1 === id).result.headOption

    this.db.run(action).map({
      case Some(f) => {
        val person = Person(f._2, f._3, f._4, f._5, f._6, f._7, f._8, f._9)
        person.id = Option[Int](f._1)
        Option[Person](person)
      }
      case None => None
    })

  }
}
