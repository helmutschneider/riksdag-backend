package se.riksdagskollen.app

case class Person(
  id: String,
  birthYear: Int,
  gender: String,
  firstName: String,
  lastName: String,
  status: String,
  party: String,
  location: String,
  databaseId: Option[BigInt] = None,
  syncId: Option[BigInt] = None
  ) extends DatabaseModel {

  def withSyncId(id: BigInt) = copy(syncId = Some(id))
  override def withDatabaseId(id: BigInt): DatabaseModel = copy(databaseId = Some(id))

}
