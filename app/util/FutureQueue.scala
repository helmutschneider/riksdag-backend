package util

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Johan on 2015-08-24.
 */
class FutureQueue[T](concurrencyLimit: Int = 1) {

  private var queue = ListBuffer[() => Future[T]]()
  private var result = ListBuffer[T]()
  private var promises = ListBuffer[Future[T]]()

  private var awaitingResponse: Int = 0

  def push(func: () => Future[T]) = queue += func

  private def runInternal(idx: Int, prom: Promise[Seq[T]]): Unit = {
    
    if ( idx >= queue.length ) {

      // wait for other futures to resolve, then resolve the global promise
      Future.sequence(promises.toSeq).map(p => {
        prom.success(this.result.toSeq)
      })

      return
    }

    awaitingResponse += 1
    val res = queue(idx)()

    promises += res

    res.map(p => {
      this.result += p
      awaitingResponse -= 1
      p
    })

    val next = idx + 1

    // if the concurrency-limit is reached we wait for the future to complete
    if ( awaitingResponse >= concurrencyLimit ) {
      res.map(p => {
        runInternal(next, prom)
        p
      })
    }
    // otherwise keep going
    else {
      runInternal(next, prom)
    }

  }

  def run(): Future[Seq[T]] = {
    val prom = Promise[Seq[T]]()
    this.runInternal(0, prom)

    prom.future
  }

}
