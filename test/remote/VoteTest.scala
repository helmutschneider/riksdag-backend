package remote

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by Johan on 2015-08-17.
 */
class VoteTest extends FunSuite with BeforeAndAfter {

  test("result enum parses string with utf8-characters correctly") {

    assert(Result.parse("avstår") == Result.Abstaining)
    assert(Result.parse("avstör") == Result.Abstaining)
    assert(Result.parse("avstr") == Result.Abstaining)
    assert(Result.parse("frööönvarande") == Result.Absent)

  }

}
