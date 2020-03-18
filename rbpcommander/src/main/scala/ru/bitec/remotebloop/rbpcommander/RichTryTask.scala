package ru.bitec.remotebloop.rbpcommander


import monix.eval.Task

import scala.util.{Failure, Success, Try}


class RichTryTask[T](val underlying: Task[Try[T]]) extends AnyVal {
   def tryFlatMap[R](body: T=>Task[Try[R]]): Task[Try[R]] = {
     underlying.flatMap[Try[R]]{
       case Failure(e) => Task.now(Failure(e))
       case Success(s) => body(s)
     }
   }
   def tryBracket[B](use: T=> Task[Try[B]])(release: T => Task[Unit]): Task[Try[B]] = {
     underlying.bracket{
       case Success(s) => use(s)
       case Failure(f) => Task.now(Failure[B](f))
     }{
       case Success(s) => release(s)
       case _ =>   Task.now()
     }
   }
  def tryMap[R](body: T=>R): Task[Try[R]] = {
    underlying.map{
      case Failure(e) => Failure(e)
      case Success(s) => Try{
        body(s)
      }
    }
  }
}

class RichTaskToTry[T](val underlying: Task[T]) extends AnyVal {
  def mapToTry[B](body: T => B):Task[Try[B]] =  {
    underlying.map{p =>
      Try{
        body(p)
      }
    }
  }
}
class RichTaskGroupTry[T](val underlying: Task[List[Try[T]]]) extends AnyVal {
  def groupByTry[B]():Task[Try[List[T]]] =  {
    underlying.map{tryList =>
      val fList = for (Failure(e) <- tryList) yield e
      if (fList.nonEmpty) {
        val runtimeException = new RuntimeException("Some task is failed")
        fList.foreach { e =>
          runtimeException.addSuppressed(e)
        }
        Failure(runtimeException)
      }else{
        Try{
          for (Success(p) <- tryList) yield p
        }
      }
    }
  }
}

object RichTryTask{
  object Implicits{
    implicit def convertToRichTryTask[T](t: Task[Try[T]]): RichTryTask[T] = {
      new RichTryTask(t)
    }
    implicit def convertToRichTaskToTry[T](t: Task[T]): RichTaskToTry[T] = {
      new RichTaskToTry(t)
    }
    implicit def convertToRichTaskGroupTry[T](t: Task[List[Try[T]]]): RichTaskGroupTry[T] = {
      new RichTaskGroupTry(t)
    }
  }
}