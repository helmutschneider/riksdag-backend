package se.riksdagskollen.app

import org.scalatest.FunSuite

/**
  * Created by johan on 2016-05-28.
  */
class ConfigParserTest extends FunSuite {

  val parser = new ConfigParser

  test("test parse") {
    val conf = parser.parse(
      """
        |QUOTED="STRING"
        |OTHER="STRING_AGAIN"
      """.stripMargin)
    assert(conf("QUOTED") == "STRING")
    assert(conf("OTHER") == "STRING_AGAIN")
  }


}
