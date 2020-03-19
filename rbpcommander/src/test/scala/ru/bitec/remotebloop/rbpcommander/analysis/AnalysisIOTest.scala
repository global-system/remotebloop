package ru.bitec.remotebloop.rbpcommander.analysis


class AnalysisIOTest extends org.scalatest.FunSuite {
   test("getZeroLastModified"){
     val result = AnalysisIO.getZeroLastModified()
     println(result)
   }
}
