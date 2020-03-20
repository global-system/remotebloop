package ru.bitec.remotebloop.rbpcommander

import java.nio.file.Path

import monix.eval.Task
import monix.reactive.Observable
import xsbti.compile.analysis.Stamp

import scala.util.Try

case class LastModifiedResult(path: Path, lastModified: Stamp)
case class HashResult(path: Path, hash: Stamp)

object FileMetaTasks {
  import TryTask.Implicits._
  import ru.bitec.remotebloop.rbpcommander.ProjectTasks.paralelism

  def lastModifiedByList(paths: List[Path]): Task[Try[List[LastModifiedResult]]]  = {
      Observable.fromIterable(paths).mapParallelUnordered(paralelism) { path =>
        TryTask{
          LastModifiedResult(path,CommanderIO.lastModifiedFromPath(path))
        }
      }.toListL.groupByTry()
  }
  def hashByList(paths: List[Path]): Task[Try[List[HashResult]]]  = {
    Observable.fromIterable(paths).mapParallelUnordered(paralelism) { path =>
      TryTask{
        HashResult(path,CommanderIO.fileHashFromPath(path))
      }
    }.toListL.groupByTry()
  }
}
