package ru.bitec.remotebloop.rbpcommander
import java.nio.file.{Path, Paths}


import monix.execution.Scheduler.Implicits.global

import scala.concurrent.{Await}
import scala.concurrent.duration._
class TasksTest extends org.scalatest.FunSuite {
  test("loadConfig"){
    val f = Tasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    assert(result.right.get.size>0)
  }
  test("safeLocalProject"){
    val f = Tasks.loadLocalProjects(Paths.get(".bloop") ).runToFuture
    val result = Await.result(f,10.seconds)
    val project = result.right.get.filter{p=> p.project.name.contains("rbpcommander")}.head
    val st = Tasks.safeLocalProject(project,Paths.get("workspace").toAbsolutePath)
    val stResult = Await.result(st.runToFuture,10.seconds)
    assert(true)
  }
}
