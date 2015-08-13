package http

import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-13.
 */
trait HttpClientTrait {

  def send(req: WSRequest): Future[WSResponse]

}
