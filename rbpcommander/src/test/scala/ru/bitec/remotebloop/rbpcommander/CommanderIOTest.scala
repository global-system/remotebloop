package ru.bitec.remotebloop.rbpcommander

import java.io.File
import java.nio.file.Paths


class CommanderIOTest extends org.scalatest.FunSuite {
   test("getZeroLastModified",ManualTag){
     val result = CommanderIO.emptyStamp()
     println(result)
   }
  test("sharedDirs"){
    val result = CommanderIO.sharedDirs()
    assert(result.size>0)
  }
  test("pathToUri",ManualTag){
    val dir = Paths.get("workspace").toAbsolutePath
    val root = dir.getParent.getParent.toAbsolutePath
    val rel = root.relativize(dir)
    val uri = rel.toUri
    assert(rel.getNameCount==2)
    println(uri.getPath)
  }
  test("filecompatibiltiy",ManualTag){
    val dir = new File("workspace/save")
    println(dir.toPath.toUri.getPath)
  }
}
