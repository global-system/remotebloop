package ru.bitec.remotebloop.rbpcommander

import java.nio.file.{Path, Paths}

import com.beust.jcommander.JCommander
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.util.{Failure, Try}
import scala.concurrent.duration._

object RbpCommander {
  val fileLockPath: Path = Paths.get("workspace/.rbpserver/lock/port.txt")

  def saveLocalProjects(bloopConfigDir:Path, targetDir:Path):Try[List[LocalProject]] = {
    val st = ProjectTasks.saveLocalProjects(
      bloopConfigDir, targetDir.toAbsolutePath
    )
    val stResult = Await.result(st.runToFuture,1.hours)
    if (stResult.isFailure) {
      val Failure(e) = stResult
      e.printStackTrace()
    }
    stResult
  }

  def restoreLocalProjects(bloopConfigDir:Path, sourceDir:Path):Try[List[LocalProject]] = {
    val restoreTask = ProjectTasks.restoreLocalProjects(
      bloopConfigDir,sourceDir.toAbsolutePath
    )
    val restoreTaskResult = Await.result(restoreTask.runToFuture,1.hours)
    if (restoreTaskResult.isFailure) {
      val Failure(e) = restoreTaskResult
      e.printStackTrace()
    }
    restoreTaskResult
  }

  def main(argArray: Array[String]): Unit = {
    val args = new RbpCommanderArgs
    val saveCmd = new RbpCommanderArgs.Save
    val restoreCmd = new RbpCommanderArgs.Restore
    val jc=JCommander.newBuilder.
      addObject(args).
      addCommand("save",saveCmd).
      addCommand("restore",restoreCmd).
      build
    jc.parse(argArray: _*)
    if (!args.help){
      val configDir = Paths.get(args.configDir).toAbsolutePath
      jc.getParsedCommand match {
        case "save" =>
          val targetDir = Paths.get(saveCmd.targetDir).toAbsolutePath
          println("Save command is started")
          println(s"Config directory:$configDir")
          println(s"Target directory:$targetDir")
          saveLocalProjects(configDir,targetDir)
        case "restore" =>
          val sourceDir = Paths.get(restoreCmd.sourceDir).toAbsolutePath
          println("Restore command is started")
          println(s"Config directory:$configDir")
          println(s"Source directory:$sourceDir")
          restoreLocalProjects(configDir,sourceDir)
        case _ =>
          println("A command is not specified, see:")
          jc.usage()
      }
    }else {
      jc.usage()
    }
  }
}
