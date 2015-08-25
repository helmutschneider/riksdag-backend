package util

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Johan on 2015-08-24.
 */
class FutureQueue[T](threads: Int = 1) {

  private var queue = ListBuffer[() => Future[T]]()
  private var result = ListBuffer[T]()
  private var promises = ListBuffer[Future[T]]()

  private var awaitingResponse: Int = 0

  def push(func: () => Future[T]) = queue += func

  private def runRecursive(idx: Int, prom: Promise[Seq[T]]): Unit = {

    // break the recursion loop if we have reached the end of the queue
    if ( idx >= queue.length ) {

      // wait for other futures to resolve, then resolve the global promise
      Future.sequence(promises.toSeq).map(p => {
        prom.success(this.result.toSeq)
      })

      return
    }

    awaitingResponse += 1
    val res = queue(idx)()

    // not all promises will resolve at the same time
    // so we need to save all of them to be sure.
    promises += res

    res.onComplete(p => {
      awaitingResponse -= 1
    })

    // after the function has completed, save its result
    res.map(p => {
      this.result += p
      p
    })

    val next = idx + 1

    awaitingResponse match {
      // if the thread-limit is reached we wait for the future to complete
      case num if num >= threads => {
        res.map(p => {
          runRecursive(next, prom)
          p
        })
      }
      // otherwise just keep going
      case _ => runRecursive(next, prom)
    }

  }

  def run(): Future[Seq[T]] = {
    val prom = Promise[Seq[T]]()
    this.runRecursive(0, prom)

    prom.future
  }

}
