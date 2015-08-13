package http

import play.api.libs.json.{Writes, Reads, Json}
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.Future
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by Johan on 2015-08-13.
 */
class HttpRepository[T <: Model](client: HttpClientTrait, url: String)(implicit reader: Reads[T], writer: Writes[T]) {

  val headers = List(
    "Content-Type" -> "application/json"
  )

  private def buildRequest(url: String): WSRequest = {
    return WS.url(url).withHeaders(headers: _*)
  }

  def get(): Future[Seq[T]] = {
    val req = this.buildRequest(this.url).withMethod("get")
    this.client.send(req).map(res => {
      res.json.as[List[T]]
    })
  }

  def get(id: Int): Future[T] = {
    val req = this.buildRequest(this.url + s"/$id").withMethod("get")
    this.client.send(req).map(res => {
      res.json.as[T]
    })
  }

  def save(item: T): Future[T] = {
    val json = Json.toJson(item)
    val req = this.buildRequest(this.url)
      .withMethod("post")
      .withBody(json)
    this.client.send(req).map(res => {
      res.json.as[T]
    })
  }

  def update(item: T): Future[T] = {
    val json = Json.toJson(item)
    val req = this.buildRequest(this.url + s"/${item.identifier}")
      .withMethod("put")
      .withBody(json)
    this.client.send(req).map(res => {
      res.json.as[T]
    })
  }

  def delete(item: T): Future[T] = {
    val req = this.buildRequest(this.url + s"/${item.identifier}")
      .withMethod("delete")
    this.client.send(req).map(res => {
      item
    })
  }

}
