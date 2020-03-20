package ru.bitec.remotebloop.rbpcommander


class CommanderIOTest extends org.scalatest.FunSuite {
   test("getZeroLastModified"){
     val result = CommanderIO.emptyStamp()
     println(result)
   }
}
