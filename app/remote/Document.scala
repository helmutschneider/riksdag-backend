package remote

import java.sql.{Timestamp, Date}

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Created by Jacob on 2015-08-15.
 */

object Document
{
  val jsonReader: Reads[Document] = (
      (JsPath \ "dok_id").read[String] and
      (JsPath \ "datum").read[Date] and
      (JsPath \ "titel").read[String] and
      (JsPath \ "dokument_url_html").read[String]
    )(Document.apply _)
}


case class Document(
                     remoteId: String,
                     publishedAt: Date,
                     title: String,
                     url: String) {

  def toDbDocument(votingId: Int): db.Document = {
    new db.Document(
      this.remoteId,
      new Timestamp(this.publishedAt.getTime),
      this.title,
      this.url,
      votingId)
  }

}
