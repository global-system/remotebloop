package ru.bitec.remotebloop.rbpcommander

import java.io.FileOutputStream
import java.nio.file.{FileSystems, Files, Path}
import java.net.URI
import java.util
import java.util.zip.ZipOutputStream


import bloop.config.Config.Project
import monix.eval.Task
import monix.reactive.Observable
import ru.bitec.remotebloop.rbpcommander.analysis.{ConfigProject, PathMapper}
import xsbti.compile.AnalysisContents

import scala.util.{Failure, Success, Try}

case class LocalProject(
                         project: Project,
                         analysisContentsOpt: Option[AnalysisContents] = None,
                         remoteCacheOpt: Option[Path] = None,
                         remoteAnalysisContentsOpt: Option[AnalysisContents] = None,
                       )


object ProjectTasks {

  import TryTask.Implicits._

  val paralelism: Int = Runtime.getRuntime.availableProcessors

  def loadLocalProjects(directory: Path): Task[Try[List[LocalProject]]] = {
    TryTask {
      (sbt.io.PathFinder(directory.toFile) * "*.json").get()
    }.tryFlatMap { files =>
      Observable.fromIterable(files).mapParallelUnordered(paralelism) { file =>
        TryTask {
          val p = ConfigProject.loadProject(file.toPath)
          LocalProject(p)
        }
      }.toListL.groupByTry()
    }
  }

  def saveLocalProjectPrepare(localProject: LocalProject, targetDir: Path,isIncremental: Boolean=false): Task[Try[LocalProject]] = TryTask {
    val fileTo = targetDir.resolve(localProject.project.name + ".zip").toFile
    if (!isIncremental&&fileTo.exists()){
      fileTo.delete()
    }
    if (!fileTo.exists()) {
      Files.createDirectories(targetDir)
      sbt.io.IO.zip(Nil,fileTo)
    }
    val analysisOpt = ConfigProject.loadLocalAnalysis(localProject.project)
    localProject.copy(
      remoteCacheOpt = Some(fileTo.toPath.toAbsolutePath),
      analysisContentsOpt = analysisOpt
    )
  }


  def saveLocalProjectAnalysis(localProject: LocalProject, targetFile: Path): Task[Try[Int]] = TryTask {
    val mapper = PathMapper.fromConfigProject(localProject.project)
    val ac = localProject.analysisContentsOpt.get
    ConfigProject.saveLocalAnalysisToPortable(
      localProject.project, targetFile, ac, mapper
    )
    1
  }

  def restoreLocalProjectAnalysis(localProject: LocalProject, targetFile: Path,metaCacheFiles: List[FilePath]): Task[Try[Int]] = TryTask {
    val mapper = PathMapper.fromConfigProject(localProject.project)
    val ac = localProject.remoteAnalysisContentsOpt.get
    ConfigProject.restoreLocalAnalysisFromPortable(
      localProject.project, targetFile, ac, mapper,metaCacheFiles
    )
    1
  }

  def saveLocalProject(localProject: LocalProject, targetDir: Path): Task[Try[LocalProject]] = {
    saveLocalProjectPrepare(localProject, targetDir.toAbsolutePath).tryFlatMap { localProject =>
      localProject.analysisContentsOpt match {
        case Some(analysisContents) =>
          TryTask {
            val urlString = ("jar:file:" + localProject.remoteCacheOpt.get.toUri.getPath)
            val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
            val rootPath = fs.getRootDirectories.iterator().next()
            val classesPath = rootPath.resolve("classes")
            if (!Files.exists(classesPath)) {
              Files.createDirectory(classesPath)
            }
            fs
          }.tryBracket { in =>
            val rootPath = in.getRootDirectories.iterator().next()
            val copyFileTask =
              analysisContents.getMiniSetup.output().getSingleOutput match {
                case o if o.isPresent =>
                  FileSyncTasks.sync(o.get().toPath, rootPath.resolve("classes"))
                case _ =>
                  Task.now(Failure(new RuntimeException("Single output is not found.")))
              }
            val copyAnalysisTask = saveLocalProjectAnalysis(localProject, rootPath.resolve("analysis.zip"))
            Task.gather(copyFileTask :: copyAnalysisTask :: Nil).groupByTry().tryMap { _ =>
              localProject
            }
          } { in =>
            Task {
              in.close()
              println(s"""Saving "${localProject.project.name}" is completed""")
            }
          }
        case None =>
          Task.now(Success(localProject))
      }
    }
  }

  def restoreLocalProject(localProject: LocalProject, sourceDir: Path): Task[Try[LocalProject]] = {
    TryTask{
      val fileTo = sourceDir.toAbsolutePath.resolve(localProject.project.name + ".zip")
      if (!Files.exists(fileTo)){
        throw  new RuntimeException("Cache file is not found")
      }
      localProject.copy(
        remoteCacheOpt = Some(fileTo.toAbsolutePath),
      )
    }.tryFlatMap{ LocalProject =>
      TryTask {
        val urlString = ("jar:file:" + LocalProject.remoteCacheOpt.get.toUri.getPath)
        val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
        fs
      }.tryBracket { in =>
        TryTask{
          val file = in.getRootDirectories.iterator().next().resolve("analysis.zip")
          val analysis=ConfigProject.loadRemoteAnalysis(localProject.project,file)
          localProject.copy(
            remoteAnalysisContentsOpt = analysis
          )
        }.tryFlatMap{ localProject =>
          val targetAnalysisFile =
            for(s <- localProject.project.scala;
                a <- s.analysis) yield (a)
          (targetAnalysisFile,localProject.remoteAnalysisContentsOpt)  match {
            case (Some(analysisFile),Some(remoteAnalysisContents)) =>
              val rootFromPath = in.getRootDirectories.iterator().next()
              val copyFileTask =
                remoteAnalysisContents.getMiniSetup.output().getSingleOutput match {
                  case o if o.isPresent =>
                    val toPath =localProject.project.out.resolve(
                      CommanderIO.keyByPath(o.get().toPath).stripPrefix("ord_root_out./")
                    );
                    Files.createDirectories(toPath)
                    FileSyncTasks.sync(rootFromPath.resolve("classes"), toPath).tryFlatMap{targetFiles =>
                      restoreLocalProjectAnalysis(localProject, analysisFile,targetFiles).tryMap{_ =>
                        localProject
                      }
                    }
                  case _ =>
                    Task.now(Failure(new RuntimeException("Single output is not found.")))
                }
                copyFileTask
            case _ =>
              TryTask.now(localProject)
          }
        }
      } { in =>
        Task {
          in.close()
          println(s"""Restoring "${localProject.project.name}" is completed""")
        }
      }
    }

  }

  def saveLocalProjects(bloopConfigDir: Path, targetDir: Path): Task[Try[List[LocalProject]]] = {
    loadLocalProjects(bloopConfigDir).tryFlatMap { projectList =>
      Observable.fromIterable(projectList).mapParallelUnordered(paralelism) { project =>
        saveLocalProject(project, targetDir)
      }.toListL.groupByTry()
    }
  }

  def restoreLocalProjects(bloopConfigDir: Path, sourceDir: Path): Task[Try[List[LocalProject]]] = {
    loadLocalProjects(bloopConfigDir).tryFlatMap { projectList =>
      Observable.fromIterable(projectList).mapParallelUnordered(paralelism) { project =>
        restoreLocalProject(project, sourceDir)
      }.toListL.groupByTry()
    }
  }

}
