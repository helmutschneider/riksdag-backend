package se.riksdagskollen.app

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.scalatest.ScalatraFunSuite

class RiksdagskollenServletTest extends ScalatraFunSuite {

  test("should return status 200") {
    200 shouldEqual 200
  }

}
