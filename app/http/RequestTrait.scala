package http

/**
 * Created by Johan on 2015-08-24.
 */
trait RequestTrait {

  def url: String
  def method: String
  def body: String
  def queryParams: Seq[(String, String)]

}
