package se.riksdagskollen.http

case class Request(
  method: String,
  url: String,
  headers: Seq[(String, String)] = Seq[(String, String)](),
  query: Seq[(String, String)] = Seq[(String, String)](),
  body: String = "") {

}
