package http

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-24.
 */
class MockClient extends HttpClientTrait {

  var respondWith: ResponseTrait = MockResponse(200, "{}")

  override def send(req: RequestTrait): Future[ResponseTrait] = {
    val promise = Promise[ResponseTrait]()

    Future {
      Thread.sleep(1000)
      promise.success(respondWith)
    }

    return promise.future
  }

}
