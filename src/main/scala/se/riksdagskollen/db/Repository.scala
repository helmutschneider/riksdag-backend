package se.riksdagskollen.db

trait Repository[T] {
  def mapToObject(data: Map[String, Any]): T
}
