package http

import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.Future
import play.api.Play.current

/**
 * Created by Johan on 2015-08-13.
 */
class HttpClient(baseUrl: String = "") extends HttpClientTrait {

  val headers = List(
    "Content-Type" -> "application/json"
  )

  def buildRequest(url: String): WSRequest = {
    WS.url(this.baseUrl + url).withHeaders(this.headers: _*)
  }

  def get(url: String): Future[WSResponse] = {
    this.buildRequest(url).get()
  }

  def post(url: String, body: String): Future[WSResponse] = {
    this.buildRequest(url).post(body)
  }

  def put(url: String, body: String): Future[WSResponse] = {
    this.buildRequest(url).put(body)
  }

  def delete(url: String): Future[WSResponse] = {
    this.buildRequest(url).delete()
  }


}
