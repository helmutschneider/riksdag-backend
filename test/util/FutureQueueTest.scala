package util

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Johan on 2015-08-25.
 */
class FutureQueueTest extends FunSuite with BeforeAndAfter with ScalaFutures {

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

    assert(p1.future.isReadyWithin(Span(550, Millis)) == false) // +550 ms (tot 550) the first promise should not be ready.
    assert(fut.isCompleted == false)
    assert(p1.future.isReadyWithin(Span(500, Millis)) == true) // +500 ms (tot 1050) the first promise should be ready
    assert(fut.isCompleted == false)
    assert(p2.future.isReadyWithin(Span(500, Millis)) == false) // +500ms (tot 1550) the second promise should not be ready
    assert(p2.future.isReadyWithin(Span(500, Millis)) == true) // +500ms (tot 2050) the second promise should be ready
    assert(fut.isReadyWithin(Span(50, Millis)) == true) // +50ms the "global" queue promise resolves last
  }

  test("Executes in parallel with concurrency above 1") {

    val p1 = Promise[Int]()
    val p2 = Promise[Int]()
    val p3 = Promise[Int]()

    val queue = new FutureQueue[Int](List(makeFunc(p1, 1, 1000), makeFunc(p2, 2, 1000), makeFunc(p3, 3, 1000)), 2)

    val fut = queue.run()

    assert(p1.future.isReadyWithin(Span(1050, Millis)) == true)
    assert(p2.future.isCompleted == true)
    assert(p3.future.isCompleted == false)
    assert(fut.isCompleted == false)
    assert(p3.future.isReadyWithin(Span(1000, Millis)) == true)
    assert(fut.isReadyWithin(Span(50, Millis)) == true)

  }

}
