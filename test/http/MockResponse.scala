package http

import play.api.libs.json.{Json, JsValue}

/**
 * Created by Johan on 2015-08-24.
 */
case class MockResponse(status: Int, body: String) extends ResponseTrait {

  override def json: JsValue = {
    Json.parse(body)
  }

}
