package db

import db.Query.Where

/**
 * Created by johan on 15-08-28.
 */
object QueryLibrary {

  lazy val latestSyncId = (new Query)
    .select("max(sync_id)")
    .from("sync")
    .where("completed_at is not null", Where.And)

}
