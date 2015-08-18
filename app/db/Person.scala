package db

import slick.driver.MySQLDriver.api._

/**
 * Created by Johan on 2015-08-18.
 */
class Person(tag: Tag) extends Table[
  (Int, String, Int, Int, String, String, String ,String, String)
  ](tag, "person") {

  def id = column[Int]("id", O.PrimaryKey)
  def personId = column[String]("person_id")
  def birthYear = column[Int]("birth_year")
  def gender = column[Int]("gender")
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def party = column[String]("party")
  def location = column[String]("location")
  def imageUrl = column[String]("image_url")

  def * = (id, personId, birthYear, gender, firstName, lastName, party, location, imageUrl)

}

object Person {
  val tableQuery = TableQuery[Person]
}
