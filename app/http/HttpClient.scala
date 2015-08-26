package http

import play.api.Application
import play.api.libs.ws.{WS, WSRequest}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by Johan on 2015-08-13.
 */
class HttpClient(implicit app: Application, ec: ExecutionContext) extends HttpClientTrait {

  def toWSRequest(req: RequestTrait): WSRequest = {
    WS.url(req.url)
      .withMethod(req.method)
      .withBody(req.body)
      .withQueryString(req.queryParams: _*)
  }

  override def send(req: RequestTrait): Future[ResponseTrait] = {
    val wsr = this.toWSRequest(req)
    wsr.execute().map(res => new Response(res))
  }

}
