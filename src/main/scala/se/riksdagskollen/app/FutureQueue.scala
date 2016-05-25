package se.riksdagskollen.app

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{ExecutionContext, Future, Promise}

class FutureQueue[T](
  funcs: Seq[() => Future[T]],
  threads: Int,
  context: ExecutionContext) {

  implicit val executionContext = context

  private val promises = funcs map { _ => Promise[T]() }
  private val awaitingResponse = new AtomicInteger()
  private val promiseAll = Future.sequence(promises map { _.future })

  private def runRecursive(idx: Int): Unit = {

    // break the recursion loop if we have reached the end of the queue
    if ( idx >= funcs.length ) {
      return
    }

    awaitingResponse.incrementAndGet()
    val fut = funcs(idx)()

    fut.map { res =>
      promises(idx).success(res)
      awaitingResponse.decrementAndGet()
    }

    val next = idx + 1

    awaitingResponse.get() match {
      // if the thread-limit is reached we wait for the future to complete
      case waiting if waiting >= threads => fut.map { _ => runRecursive(next) }
      // otherwise just keep going
      case _ => runRecursive(next)
    }

  }

  def run(): FutureQueue[T] = {
    this.runRecursive(0)
    this
  }

  def all(): Future[Seq[T]] = {
    promiseAll
  }

  def one[U](idx: Int): Future[T] = {
    promises(idx).future
  }

}
