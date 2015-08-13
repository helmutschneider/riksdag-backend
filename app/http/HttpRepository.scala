package http

import play.api.libs.json.{Writes, Reads, Json}

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-13.
 */
class HttpRepository[T <: Model](client: HttpClientTrait, url: String)(implicit reader: Reads[T], writer: Writes[T]) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def get(): Future[Seq[T]] = {
    this.client.get(this.url).map(res => {
      res.json.as[List[T]]
    })
  }

  def get(id: Int): Future[T] = {
    this.client.get(this.url + s"/$id").map(res => {
      res.json.as[T]
    })
  }

  def save(item: T): Future[T] = {
    val json = Json.toJson(item)

    this.client.post(this.url, json.toString()).map(res => {
      res.json.as[T]
    })
  }

  def update(item: T): Future[T] = {
    val json = Json.toJson(item)

    this.client.put(this.url + s"/${item.identifier}", json.toString()).map(res => {
      res.json.as[T]
    })
  }

  def delete(item: T): Future[T] = {
    this.client.delete(this.url + s"/${item.identifier}").map(res => {
      item
    })
  }

}
