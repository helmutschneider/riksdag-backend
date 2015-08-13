package http

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-13.
 */
trait HttpClientTrait {

  def get(url: String): Future[WSResponse]
  def post(url: String, body: String): Future[WSResponse]
  def put(url: String, body: String): Future[WSResponse]
  def delete(url: String): Future[WSResponse]

}
