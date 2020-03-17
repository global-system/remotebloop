package ru.bitec.remotebloop.rbpcommander

import monix.execution.Scheduler.Implicits.global
import java.io.{File, FileOutputStream}
import java.nio.file.{FileSystems, Files, Path}
import java.net.URI
import java.util
import java.util.zip.ZipOutputStream

import bloop.config.Config
import monix.eval.Task
import monix.reactive.{Observable, OverflowStrategy}
import sbt.internal.inc.FileAnalysisStore

object Tasks {
  val paralelism: Int = Runtime.getRuntime.availableProcessors

  def save[T](body: => T): Either[Throwable, T] = {
    try {
      Right(body)
    } catch {
      case e: Exception => Left(e)
    }
  }

  def loadLocalProjects(path: Path): Task[Either[List[Throwable], List[LocalProject]]] = {
    Observable.create[Either[Throwable, File]](OverflowStrategy.Unbounded) { s =>
      Task{
        save {
          (sbt.io.PathFinder(path.toFile) * ("*.json")).get().foreach(f =>
            s.onNext(Right(f))
          )
        } match {
          case Right(_) => s.onComplete()
          case Left(e) => s.onError(e)
        }
      }.executeAsync.runToFuture
    }.mapParallelUnordered(paralelism) { fileEither =>
      Task[Either[Throwable, Config.File]] {
        fileEither match {
          case Right(f) =>
            import bloop.config.read
            val config = read(f.toPath)
            config
          case Left(e) => Left(e)
        }
      }
    }.toListL.map { configEitherList =>
      val eList = for (Left(e) <- configEitherList) yield e
      if (eList.size > 0) {
        Left(eList)
      } else {
        Right(
          for (Right(p) <- configEitherList) yield LocalProject(project = p.project)
        )
      }
    }
  }
  def safeLocalProjectPrepare(localProject: LocalProject,pathTo: Path): Task[Either[Throwable,LocalProject]] = Task{
    val analysis = (for (s <- localProject.project.scala; a <- s.analysis) yield {
      val file = a.toFile
      if (file.exists()) {
        Some(file)
      } else {
        None
      }
    }).flatten
    val fileTo = pathTo.resolve(localProject.project.name+".zip").toFile;
    val fos = new FileOutputStream(fileTo)
    val zos = new ZipOutputStream(fos)
    zos.close()
    analysis match {
      case Some(file) =>
        save{
          val remoteStore = FileAnalysisStore.binary(file)
          localProject.copy(
            remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath),
            analysisContentsOpt = remoteStore.get() match {
              case v if v.isPresent => Some(v.get())
              case _ => None
            }
          )
        }
      case None =>
        Right(localProject.copy(remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath)))
    }
  }
  def safeLocalProject(localProject: LocalProject,pathTo: Path): Task[Either[List[Throwable],LocalProject]] = {
    safeLocalProjectPrepare(localProject,pathTo).flatMap{
      case Right(localProject) =>
        localProject.analysisContentsOpt match {
          case Some(analysisContents) =>
            Task {
              val urlString = ("jar:file:/" + localProject.remoteCacheOpt.get).replace('\\','/')
              val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
              fs
            }.bracket{ in =>
              val t1 = Task[Either[Throwable, LocalProject]] {
                save{
                  analysisContents.getMiniSetup.output().getSingleOutput match {
                    case o if o.isPresent =>
                      Files.copy(o.get().toPath,in.getRootDirectories.iterator.next())
                    case _ =>
                      throw  new RuntimeException("Single output is not found.")
                  }
                 //
                  localProject
                }
              }
              val t2 = Task[Either[Throwable, LocalProject]] {
                Right(localProject)
              }
              Task.gather(t1 :: t2 :: Nil).map { eitherList =>
                val throwList = for (Left(t) <- eitherList) yield t
                if (throwList.nonEmpty) {
                  Left(throwList)
                } else {
                  Right(localProject)
                }
              }
            } { in =>
              Task{
                in.close()
              }
            }
          case None =>
            Task.now(Right(localProject))
        }
      case Left(e) => Task.now(Left(e :: Nil))
    }
  }
}
