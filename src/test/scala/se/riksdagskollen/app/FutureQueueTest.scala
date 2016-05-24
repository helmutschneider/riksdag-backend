package se.riksdagskollen.app

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatra.test.scalatest.ScalatraFunSuite

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Johan on 2015-08-25.
 */
class FutureQueueTest extends ScalatraFunSuite with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  private def makeFunc(p: Promise[Int], resolveWith: Int, sleep: Int) = {
    () => {
      Future {
        Thread.sleep(sleep)
        p.success(resolveWith)
        println(s"Resolved with ${resolveWith}")
      }

      p.future
    }
  }

  test("Executes sequentially when concurrency is 1") {

    val p1 = Promise[Int]()
    val p2 = Promise[Int]()

    val queue = new FutureQueue[Int](List(makeFunc(p1, 1, 1000), makeFunc(p2, 2, 1000)), 1)
    val fut = queue.run()

    assert(p1.future.isCompleted == false) // +250 ms (tot 250) the first promise should not be ready.
    assert(p1.future.isReadyWithin(Span(1500, Millis)) == true) // +1500 ms (tot 1500) the first promise should be ready
    assert(p2.future.isCompleted == false) // +0 (tot 1500) the second promise should not be ready
    assert(p2.future.isReadyWithin(Span(1500, Millis)) == true) // +1000ms (tot 2500) the second promise should be ready
    assert(fut.isCompleted == true) // +0ms the "global" queue promise resolves last
  }

  test("Executes in parallel with concurrency above 1") {

    val p1 = Promise[Int]()
    val p2 = Promise[Int]()

    val queue = new FutureQueue[Int](List(makeFunc(p1, 1, 1000), makeFunc(p2, 2, 1000)), 2)

    val fut = queue.run()

    assert(p1.future.isReadyWithin(Span(2000, Millis)) == true)
    assert(p2.future.isCompleted == true)
    assert(fut.isCompleted == true)

  }

}
