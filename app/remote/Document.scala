package remote

import java.sql.Date

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Created by Jacob on 2015-08-15.
 */

object Document
{
  val jsonReader: Reads[Document] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "publicerad").read[Date] and
      (JsPath \ "titel").read[String] and
      (JsPath \ "undertitel").read[String] and
      (JsPath \ "typ").read[String] and
      (JsPath \ "subtyp").read[String] and
      (JsPath \ "dokument_url_html").read[String] and
      (JsPath \ "dokument_url_text").read[String]
    )(Document.apply _)
}


case class Document(
                     remoteId: String,
                     docDate: Date,
                     title: String,
                     subTitle: String,
                     `type`: String,
                     subType: String,
                     urlHtml: String,
                     urlText: String)
