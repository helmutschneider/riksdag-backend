package se.riksdagskollen.http

import org.json4s.JValue
import org.json4s.jackson.JsonMethods

case class Response(
  statusCode: Int,
  headers: Seq[(String, String)] = Seq[(String, String)](),
  body: String = "") {

  def isSuccess: Boolean = statusCode < 400
  def json: JValue = JsonMethods.parse(body)

}
