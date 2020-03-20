package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Paths

class TestAnalysisTest extends org.scalatest.FunSuite {
  test("read"){
    val fromFile = Paths.get("rbpserver/target/streams/compile/bloopAnalysisOut/_global/streams/inc_compile_2.12.zip")
    TestAnalysis.traceReadAnalysis(fromFile)
  }
  test("readPortable"){
    val fromFile = Paths.get("workspace/save/analysis.zip")
    val a = TestAnalysis.traceReadAnalysis(fromFile)
    assert(a!=null)
  }
}
