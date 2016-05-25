package se.riksdagskollen.http

case class Response(
  statusCode: Int,
  headers: Seq[(String, String)] = Seq[(String, String)](),
  body: String = "") {

  def isSuccess: Boolean = statusCode < 400

}
