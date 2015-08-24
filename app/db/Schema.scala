package db

import org.squeryl.annotations.Column

/**
 * Created by Johan on 2015-08-23.
 */


class Person(val id: Int,
             @Column("person_id") val personId: String,
             @Column("birth_year") val birthYear: Int,
             val gender: Int,
             @Column("first_name") val firstName: String,
             @Column("last_name") val lastName: String,
             val party: String,
             val location: String,
             @Column("image_url") val imageUrl: String) {

}


object Schema extends org.squeryl.Schema {

  val people = table[Person]("person")

}
