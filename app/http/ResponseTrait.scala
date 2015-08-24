package http

import play.api.libs.json.JsValue

/**
 * Created by Johan on 2015-08-24.
 */
trait ResponseTrait {

  def json : JsValue
  def body : String
  def status : Int

}
