package se.riksdagskollen.app

case class Vote(
  value: String,
  regarding: String,
  databaseId: Option[BigInt] = None,
  votingDatabaseId: Option[BigInt] = None,
  personDatabaseId: Option[BigInt] = None) extends DatabaseModel {

  override def withDatabaseId(id: BigInt) = copy(databaseId = Some(id))
  def withVotingDatabaseId(id: BigInt) = copy(votingDatabaseId = Some(id))
  def withPersonDatabaseId(id: BigInt) = copy(personDatabaseId = Some(id))

}
