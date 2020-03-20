package ru.bitec.remotebloop.rbpcommander
import java.nio.file.{Path, Paths}

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure
class ProjectTasksTest extends org.scalatest.FunSuite {
  test("loadConfig"){
    val f = ProjectTasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    assert(result.get.size>0)
  }
  test("saveLocalProject"){
    val f = ProjectTasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.get.filter{p=> p.project.name.equals("rbpcommander")}.head
    val st = ProjectTasks.saveLocalProject(project,Paths.get("workspace/save").toAbsolutePath)
    val stResult = Await.result(st.runToFuture,1.hours)
    stResult match {
      case Failure(e) =>
        e.printStackTrace()
      case _ =>
    }
    assert(stResult.isSuccess)
  }
  test("restoreLocalProject"){
    val f = ProjectTasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.get.filter{p=> p.project.name.equals("rbpcommander")}.head
    val st = ProjectTasks.restoreLocalProject(project,Paths.get("workspace/save").toAbsolutePath)
    val stResult = Await.result(st.runToFuture,1.hours)
    stResult match {
      case Failure(e) =>
        e.printStackTrace()
      case _ =>
    }
    assert(stResult.isSuccess)
  }
  test("saveLocalProjects"){
    val st = ProjectTasks.saveLocalProjects(
      Paths.get(".bloop"),
      Paths.get("workspace/save").toAbsolutePath
    )
    val stResult = Await.result(st.runToFuture,1.hours)
    if (stResult.isFailure) {
      val Failure(e) = stResult
      e.printStackTrace()
    }
    assert(stResult.isSuccess)
    //println("press any key to exit")
    //System.in.read()
  }
}
