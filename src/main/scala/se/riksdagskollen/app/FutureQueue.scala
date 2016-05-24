package se.riksdagskollen.app

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{ExecutionContext, Promise, Future}

/**
 * Created by Johan on 2015-08-24.
 */
class FutureQueue[T](funcs: Seq[() => Future[T]], threads: Int = 1)(implicit ec: ExecutionContext) {

  private val promises = funcs map (p => Promise[T]())
  private val awaitingResponse = new AtomicInteger()

  private def runRecursive(idx: Int): Unit = {

    // break the recursion loop if we have reached the end of the queue
    if ( idx >= funcs.length ) {
      return
    }

    awaitingResponse.incrementAndGet()
    val fut = funcs(idx)()

    fut map { res =>
      promises(idx).success(res)
      awaitingResponse.decrementAndGet()
    }

    val next = idx + 1

    awaitingResponse.get() match {
      // if the thread-limit is reached we wait for the future to complete
      case x if x >= threads => fut map { _ => runRecursive(next) }
      // otherwise just keep going
      case _ => runRecursive(next)
    }

  }

  def run(): Future[Seq[T]] = {
    this.runRecursive(0)

    Future.sequence(promises map(_.future))
  }

}
