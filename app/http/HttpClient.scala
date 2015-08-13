package http

import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.Future

/**
 * Created by Johan on 2015-08-13.
 */
class HttpClient extends HttpClientTrait {

  def send(req: WSRequest): Future[WSResponse] = {
    return req.execute()
  }

}
