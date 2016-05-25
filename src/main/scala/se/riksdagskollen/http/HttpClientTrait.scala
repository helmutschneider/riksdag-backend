package se.riksdagskollen.http

import scala.concurrent.Future

trait HttpClientTrait {

  def send(req: Request): Future[Response]

}
