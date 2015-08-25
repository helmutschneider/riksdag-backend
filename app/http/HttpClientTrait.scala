package http

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-13.
 */
trait HttpClientTrait {

  def send(req: RequestTrait): Future[ResponseTrait]

}
