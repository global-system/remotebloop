package ru.bitec.remotebloop.rbpcommander

import monix.eval.Task

import scala.util.Try

object TryTask {
  def apply[T](body: => T):Task[Try[T]] = {
    Task.eval(Try(body))
  }
}
