package remote

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-24.
 */
trait RepositoryTrait[T] {

  def fetch(): Future[Seq[T]]

}
