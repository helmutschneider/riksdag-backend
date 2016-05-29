package se.riksdagskollen.http

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http._

class ScalajHttpClient(context: ExecutionContext) extends HttpClientTrait {

  private val sendBodyWithMethods = Seq[String]("post", "put", "patch", "delete")

  override def send(req: Request): Future[Response] = {
    var scalajRequest = Http(req.url)
      .method(req.method)
      .headers(req.headers)
      .params(req.query)
      .timeout(1000, 30000)

    if (sendBodyWithMethods.contains(req.method.toLowerCase())) {
      scalajRequest = scalajRequest.postData(req.body)
        .method(req.method) // postData changes the method, reset it
    }

    Future {
      val scalajResponse = scalajRequest.asString
      Response(
        scalajResponse.code,
        (scalajResponse.headers map { kv => kv._1 -> kv._2.mkString(";") }).toSeq,
        scalajResponse.body
      )
    }(context)
  }
}
