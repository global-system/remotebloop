package ru.bitec.remotebloop.rbpcommander

import java.net.URI

import monix.execution.Scheduler.Implicits.global
import java.nio.file.{FileSystems, Paths}
import java.util

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure

class FileSynchronizerTest extends org.scalatest.FunSuite {
  test("scanPath"){
    val task = FileSynchronizer.scanPath(Paths.get("rbpcommander"))
    val result = Await.result(task.runToFuture,10.seconds)
    assert(result.isSuccess)
  }
  test("scanZipPath"){
    val urlString = ("jar:file:/" + Paths.get("workspace/commons-daemon.zip").toAbsolutePath).replace('\\','/')
    val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
    try{
      val task = FileSynchronizer.scanPath(fs.getRootDirectories.iterator().next())
      val result = Await.result(task.runToFuture,10.seconds)
      assert(result.isSuccess)
    }finally{
      fs.close()
    }
  }
  test("syncZipPath"){
    val urlString = ("jar:file:/" + Paths.get("workspace/test.zip").toAbsolutePath).replace('\\','/')
    val fs = FileSystems.newFileSystem(URI.create(urlString), new util.HashMap[String, AnyRef])
    try{
      val task = FileSynchronizer.sync(Paths.get("project"),fs.getRootDirectories.iterator().next())
      val result = Await.result(task.runToFuture,10.seconds)
      if (result.isFailure){
        val Failure(e) = result
        e.printStackTrace()
      }
      assert(result.isSuccess)
    }finally{
      fs.close()
    }
  }
}
