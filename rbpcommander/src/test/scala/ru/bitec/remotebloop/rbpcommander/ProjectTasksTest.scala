package ru.bitec.remotebloop.rbpcommander
import java.nio.file.{Path, Paths}

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure
class ProjectTasksTest extends org.scalatest.FunSuite {
  val bloopConfigDir = Paths.get(".bloop")
  val workSpaceDir = Paths.get("workspace/save")
  test("loadConfig"){
    val f = ProjectTasks.loadLocalProjects(bloopConfigDir).runToFuture
    val result = Await.result(f,10.seconds)
    assert(result.get.size>0)
  }

  test("saveLocalProject",ManualTag){
    val f = ProjectTasks.loadLocalProjects(bloopConfigDir).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.get.filter{p=> p.project.name.equals("rbpcommander")}.head
    val st = ProjectTasks.saveLocalProject(project,workSpaceDir)
    val stResult = Await.result(st.runToFuture,1.hours)
    stResult match {
      case Failure(e) =>
        e.printStackTrace()
      case _ =>
    }
    assert(stResult.isSuccess)
  }
  test("restoreLocalProject",ManualTag){
    val f = ProjectTasks.loadLocalProjects(bloopConfigDir ).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.get.filter{p=> p.project.name.equals("rbpcommander")}.head
    val st = ProjectTasks.restoreLocalProject(project,workSpaceDir)
    val stResult = Await.result(st.runToFuture,1.hours)
    stResult match {
      case Failure(e) =>
        e.printStackTrace()
      case _ =>
    }
    assert(stResult.isSuccess)
  }



  test("saveLocalProjects",ManualTag){
    val stResult = RbpCommander.saveLocalProjects(bloopConfigDir,workSpaceDir)
    assert(stResult.isSuccess)
  }



  test("restoreLocalProjects",ManualTag){
    val restoreTaskResult = RbpCommander.restoreLocalProjects(bloopConfigDir,workSpaceDir)
    assert(restoreTaskResult.isSuccess)
  }

  test("saveAndRestoreLocalProjects"){
    val saveTaskResult  = RbpCommander.saveLocalProjects(bloopConfigDir,workSpaceDir)
    assert(saveTaskResult.isSuccess)
    val restoreTaskResult = RbpCommander.restoreLocalProjects(bloopConfigDir,workSpaceDir)
    assert(restoreTaskResult.isSuccess)
  }
}
