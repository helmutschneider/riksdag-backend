package se.riksdagskollen.app

trait DatabaseModel {

  def isNewRecord: Boolean = {
    databaseId match {
      case Some(x) => false
      case _ => true
    }
  }

  def databaseId: Option[BigInt]
  def withDatabaseId(id: BigInt): DatabaseModel

}
