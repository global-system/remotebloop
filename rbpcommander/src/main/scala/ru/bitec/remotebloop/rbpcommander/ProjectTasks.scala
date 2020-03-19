package ru.bitec.remotebloop.rbpcommander

import java.io.FileOutputStream
import java.nio.file.{FileSystems, Path}
import java.net.URI
import java.util
import java.util.zip.ZipOutputStream

import bloop.config.Config
import bloop.config.Config.{JvmConfig, Project}
import monix.eval.Task
import monix.reactive.Observable
import ru.bitec.remotebloop.rbpcommander.analysis.{ConfigProject, RootDir}
import sbt.internal.inc.FileAnalysisStore
import xsbti.compile.AnalysisContents

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

case class LocalProject(
                         project: Project,
                         analysisContentsOpt: Option[AnalysisContents]=None,
                         remoteCacheOpt: Option[Path]=None)

case class RemoteProject(project: Project, analysisContentsOpt: Option[AnalysisContents])

object ProjectTasks {
  import RichTryTask.Implicits._
  val paralelism: Int = Runtime.getRuntime.availableProcessors

  def loadLocalProjects(path: Path): Task[Try[List[LocalProject]]] = {
    TryTask{
      (sbt.io.PathFinder(path.toFile) * "*.json").get()
    }.tryFlatMap{files =>
        Observable.fromIterable(files).mapParallelUnordered(paralelism) { file =>
          TryTask{
            val p = ConfigProject.loadProject(file.toPath)
            LocalProject(p)
          }
        }.toListL.groupByTry()
    }
  }
  def saveLocalProjectPrepare(localProject: LocalProject, pathTo: Path): Task[Try[LocalProject]] = TryTask{
    val fileTo = pathTo.resolve(localProject.project.name+".zip").toFile
    if (!fileTo.exists()){
      for(
        fos<- Using( new FileOutputStream(fileTo));
        _ <- Using(new ZipOutputStream(fos))){
      }
    }
    val analysisOpt = ConfigProject.loadLocalAnalysis(localProject.project)
    localProject.copy(
      remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath),
      analysisContentsOpt = analysisOpt
    )
  }
  def saveLocalProject(localProject: LocalProject, pathTo: Path): Task[Try[LocalProject]] = {
    saveLocalProjectPrepare(localProject,pathTo).tryFlatMap{ localProject =>
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
                      FileSyncTasks.sync(o.get().toPath,in.getRootDirectories.iterator().next())
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
  def saveLocalProjects(bloopPath: Path,targetPath: Path):Task[Try[List[LocalProject]]] = {
    loadLocalProjects(bloopPath).tryFlatMap{ projectList =>
      Observable.fromIterable(projectList).mapParallelUnordered(paralelism) { project =>
        saveLocalProject(project,targetPath)
      }.toListL.groupByTry()
    }
  }

  def saveLocalProjectAnalysis(localProject: LocalProject):Task[Try[Int]] = {
    val prepareRootsTask = TryTask{


    }
    TryTask{0}
  }
}