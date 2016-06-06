package se.riksdagskollen.app

case class Vote(
  value: Int,
  regarding: Int,
  votingId: String,
  personId: String) extends Model {

  override def toMap = Map(
    "value" -> value,
    "regarding" -> regarding,
    "voting_id" -> votingId,
    "person_id" -> personId
  )

}

object Vote {
  object Value {
    def parse(value: String): Int = {
      value.replaceAll("[^\\w]", "").toLowerCase() match {
        case "nej" => 0
        case "ja" => 1
        case "avstr" => 2
        case "frnvarande" => 3
        case _ => 4
      }
    }
  }

  object Regarding {
    def parse(value: String): Int = {
      value.replaceAll("[^\\w]", "").toLowerCase() match {
        case "sakfrgan" => 0
        case "motivfrgan" => 1
        case _ => 2
      }
    }
  }
}
