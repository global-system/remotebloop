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
  val paralelism: Int = Runtime.getRuntime.availableProcessors

  def loadLocalProjects(path: Path): Task[Try[List[LocalProject]]] = {
    Task{Try{
      (sbt.io.PathFinder(path.toFile) * ("*.json")).get()
    }}.flatMap{
      case Success(files) =>
        Observable.fromIterable(files).mapParallelUnordered(paralelism) { file =>
          Task[Try[Config.File]] {
            import bloop.config.read
            val config = read(file.toPath)
            config.fold(Failure(_),Success(_))
          }
        }.toListL.map[Try[List[LocalProject]]]{ configTryList =>
          Try {
            val fList = for (Failure(e) <- configTryList) yield e
            if (fList.nonEmpty) {
              val runtimeException = new RuntimeException("loadLocalProjects is faild")
              fList.foreach { e =>
                runtimeException.addSuppressed(e)
              }
              throw runtimeException
            }
            for (Success(p) <- configTryList) yield LocalProject(project = p.project)
          }
        }
      case Failure(e) => Task.now(Failure(e))
    }
  }
  def safeLocalProjectPrepare(localProject: LocalProject,pathTo: Path): Task[Try[LocalProject]] = Task{Try{
    val analysis = (for (s <- localProject.project.scala; a <- s.analysis) yield {
      val file = a.toFile
      if (file.exists()) {
        Some(file)
      } else {
        None
      }
    }).flatten
    val fileTo = pathTo.resolve(localProject.project.name+".zip").toFile;
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
  }}
  def safeLocalProject(localProject: LocalProject,pathTo: Path): Task[Try[LocalProject]] = {
    safeLocalProjectPrepare(localProject,pathTo).flatMap{
      case Success(localProject) =>
        localProject.analysisContentsOpt match {
          case Some(analysisContents) =>
            Task {
              val fileTo = localProject.remoteCacheOpt.get.toFile
              val fos = new FileOutputStream(fileTo)
              val zos = new ZipOutputStream(fos)
              zos.close()
              fos.close()
              val urlString = ("jar:file:/" + localProject.remoteCacheOpt.get).replace('\\','/')
              val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
              fs
            }.bracket{ in =>
              val t1 = Task[Try[LocalProject]] {Try{
                  analysisContents.getMiniSetup.output().getSingleOutput match {
                    case o if o.isPresent =>
                      Files.copy(o.get().toPath,in.getRootDirectories.iterator.next())
                    case _ =>
                      throw  new RuntimeException("Single output is not found.")
                  }
                  //
                  localProject
                }}

              val t2 = Task[Try[LocalProject]]{
                Try(localProject)
              }
              Task.gather(t1 :: t2 :: Nil).map { eitherList =>
                val throwList = for (Failure(t) <- eitherList) yield t
                if (throwList.nonEmpty) {
                  val re = new RuntimeException()
                  throwList.foreach{e =>
                    re.addSuppressed(e)
                  }
                  Failure(re)
                } else {
                  Success(localProject)
                }
              }
            } { in =>
              Task{
                in.close()
              }
            }
          case None =>
            Task.now(Success(localProject))
        }
      case Failure(e) => Task.now(Failure(e))
    }
  }
}
