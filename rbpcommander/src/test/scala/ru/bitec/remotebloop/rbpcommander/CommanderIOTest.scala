package ru.bitec.remotebloop.rbpcommander


class CommanderIOTest extends org.scalatest.FunSuite {
   test("getZeroLastModified"){
     val result = CommanderIO.emptyStamp()
     println(result)
   }
  test("sharedDirs"){
    val result = CommanderIO.sharedDirs()
    assert(result.size>0)
  }
}
