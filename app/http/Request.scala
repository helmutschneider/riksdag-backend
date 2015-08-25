package http

/**
 * Created by Johan on 2015-08-24.
 */
case class Request(url: String, method: String, body: String, queryParams: Seq[(String, String)]) extends RequestTrait {

  def this(url: String) = this(url, "GET", "", List[(String, String)]())

}
