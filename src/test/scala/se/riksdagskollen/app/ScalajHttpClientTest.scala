package se.riksdagskollen.app

import se.riksdagskollen.http._
import scala.concurrent.ExecutionContext

class ScalajHttpClientTest extends AbstractHttpClientTest {

  override def getHttpClient: HttpClientTrait = {
    new ScalajHttpClient(ExecutionContext.global)
  }

}
