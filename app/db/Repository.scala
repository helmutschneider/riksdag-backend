package db

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-18.
 */
trait Repository[T] {

  def findAll(): Future[Seq[T]]
  def findOne(id: Int): Future[Option[T]]
  def save(item: T): Future[Boolean]
  def save(items: Seq[T]): Future[Boolean]
  //def delete(obj: T): Boolean

}
