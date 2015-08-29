/**
 * Created by Johan on 2015-08-29.
 */

import javax.inject.Inject
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter

class Filters @Inject() (cors: CORSFilter) extends HttpFilters {

  def filters = Seq(cors)

}
