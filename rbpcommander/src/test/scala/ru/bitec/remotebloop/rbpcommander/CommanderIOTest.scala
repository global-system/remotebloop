package ru.bitec.remotebloop.rbpcommander

import java.nio.file.Paths


class CommanderIOTest extends org.scalatest.FunSuite {
   test("getZeroLastModified"){
     val result = CommanderIO.emptyStamp()
     println(result)
   }
  test("sharedDirs"){
    val result = CommanderIO.sharedDirs()
    assert(result.size>0)
  }
  test("pathToUri"){
    val dir = Paths.get("workspace").toAbsolutePath
    val root = dir.getParent.getParent.toAbsolutePath
    val rel = root.relativize(dir)
    val uri = rel.toUri
    assert(rel.getNameCount==2)
    println(uri.getPath)
  }
}
