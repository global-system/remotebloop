package ru.bitec.remotebloop.rbpcommander

import monix.execution.Scheduler.Implicits.global
import java.io.{File, FileOutputStream}
import java.nio.file.{FileSystems, Files, Path, Paths}
import java.net.URI
import java.util
import java.util.zip.ZipOutputStream

import bloop.config.Config
import monix.eval.Task
import monix.reactive.{Observable, OverflowStrategy}
import sbt.internal.inc.FileAnalysisStore

import scala.util.{Failure, Success, Try}

object Tasks {
  import RichTryTask.Implicits._
  val paralelism: Int = Runtime.getRuntime.availableProcessors

  def loadLocalProjects(path: Path): Task[Try[List[LocalProject]]] = {
    TryTask{
      (sbt.io.PathFinder(path.toFile) * ("*.json")).get()
    }.tryFlatMap{files =>
        Observable.fromIterable(files).mapParallelUnordered(paralelism) { file =>
          Task[Try[Config.File]] {
            import bloop.config.read
            val config = read(file.toPath)
            config.fold(Failure(_),Success(_))
          }
        }.toListL.groupByTry().tryMap{ list =>
          list.map{i=> LocalProject(i.project)}
        }
    }
  }
  def safeLocalProjectPrepare(localProject: LocalProject,pathTo: Path): Task[Try[LocalProject]] = TryTask{
    val analysis = (for (s <- localProject.project.scala; a <- s.analysis) yield {
      val file = a.toFile
      if (file.exists()) {
        Some(file)
      } else {
        None
      }
    }).flatten
    val fileTo = pathTo.resolve(localProject.project.name+".zip").toFile
    for(
      fos<- Using( new FileOutputStream(fileTo));
      _ <- Using(new ZipOutputStream(fos))){
    }
    analysis match {
      case Some(file) =>
          val remoteStore = FileAnalysisStore.binary(file)
          localProject.copy(
            remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath),
            analysisContentsOpt = remoteStore.get() match {
              case v if v.isPresent => Some(v.get())
              case _ => None
            }
          )
      case None =>
        localProject.copy(remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath))
    }
  }
  def safeLocalProject(localProject: LocalProject,pathTo: Path): Task[Try[LocalProject]] = {
    safeLocalProjectPrepare(localProject,pathTo).tryFlatMap{ localProject =>
        localProject.analysisContentsOpt match {
          case Some(analysisContents) =>
            TryTask {
              val urlString = ("jar:file:/" + localProject.remoteCacheOpt.get).replace('\\','/')
              val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
              fs
            }.tryBracket{ in =>
              val copyFileTask =
                  analysisContents.getMiniSetup.output().getSingleOutput match {
                    case o if o.isPresent =>
                      FileSynchronizer.sync(o.get().toPath,in.getRootDirectories.iterator().next())
                    case _ =>
                      Task.now(Failure(new RuntimeException("Single output is not found.")))
                  }
              val copyAnalysisTask = TryTask{
                 localProject
              }
              Task.gather(copyFileTask :: copyAnalysisTask :: Nil).groupByTry().tryMap{ _ =>
                localProject
              }
            } { in =>
              Task{
                in.close()
              }
            }
          case None =>
            Task.now(Success(localProject))
        }
    }
  }
}
