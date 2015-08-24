package db

import java.sql.Timestamp

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

/**
 * Created by Johan on 2015-08-24.
 */
class Document(@Column("remote_id") val remoteId: String,
               @Column("published_at") val publishedAt: Timestamp,
               val title: String,
               @Column("subtitle") val subTitle: String,
               val `type`: String,
               @Column("subtype") val subType: String,
               @Column("url_html") val urlHtml: String,
               @Column("url_text") val urlText: String,
               @Column("sync_id") val syncId: Int
                ) extends KeyedEntity[Int] {

  @Column("document_id")
  var id: Int = 0

}
