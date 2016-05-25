package se.riksdagskollen.app

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.{Future}

class FutureQueueTest extends FunSuite with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  private def makeFunc[T](resolveWith: T, sleep: Int): () => Future[T] = {
    () => {
      Future {
        Thread.sleep(sleep)
        println(s"Resolved with ${resolveWith}")
        resolveWith
      }(scala.concurrent.ExecutionContext.global)
    }
  }

  test("Executes sequentially when concurrency is 1") {

    val queue = new FutureQueue[Int](
      Seq(makeFunc(1, 1000), makeFunc(2, 1000)),
      1,
      scala.concurrent.ExecutionContext.global
    )

    queue.run()
    val p1 = queue.one(0)
    val p2 = queue.one(1)

    assert(p1.isCompleted == false) // +250 ms (tot 250) the first promise should not be ready.
    assert(p1.isReadyWithin(Span(1500, Millis))) // +1500 ms (tot 1500) the first promise should be ready
    assert(p2.isCompleted == false) // +0 (tot 1500) the second promise should not be ready
    assert(p2.isReadyWithin(Span(1500, Millis))) // +1000ms (tot 2500) the second promise should be ready
    assert(queue.all().isCompleted)
  }

  test("Executes in parallel with concurrency above 1") {

    val queue = new FutureQueue[Int](
      Seq(makeFunc(1, 1000), makeFunc(2, 1000)),
      2,
      scala.concurrent.ExecutionContext.global
    )

    val fut = queue.run()
    val p1 = queue.one(0)
    val p2 = queue.one(1)

    assert(p1.isReadyWithin(Span(2000, Millis)))
    assert(p2.isCompleted)
    assert(queue.all().isCompleted)
  }

}
