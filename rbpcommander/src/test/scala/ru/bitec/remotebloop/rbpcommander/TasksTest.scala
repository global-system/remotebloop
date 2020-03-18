package ru.bitec.remotebloop.rbpcommander
import java.nio.file.{Path, Paths}

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure
class TasksTest extends org.scalatest.FunSuite {
  test("loadConfig"){
    val f = Tasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    assert(result.get.size>0)
  }
  test("safeLocalProject"){
    val f = Tasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.get.filter{p=> p.project.name.equals("rbpcommander")}.head
    val st = Tasks.safeLocalProject(project,Paths.get("workspace").toAbsolutePath)
    val stResult = Await.result(st.runToFuture,1.hours)
    stResult match {
      case Failure(e) =>
        e.printStackTrace()
      case _ =>
    }
    assert(stResult.isSuccess)
  }
}
