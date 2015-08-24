package http

import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse


/**
 * Created by Johan on 2015-08-24.
 */
class Response(underlying: WSResponse) extends ResponseTrait {

  override def json: JsValue = underlying.json
  override def body: String = underlying.body
  override def status: Int = underlying.status

}
