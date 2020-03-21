package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Paths

import ru.bitec.remotebloop.rbpcommander.ManualTag

class ConfigProjectTest extends org.scalatest.FunSuite {
  val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
  test("getPathMapper") {
    val resutl = PathMapper.fromConfigProject(project)
    assert(resutl.inRootDirMaps.mapByKey.nonEmpty)
  }
  test("saveLocalAnalysisToPortable",ManualTag) {
    val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
    val pathTo = Paths.get("workspace/save/rbpcommander.prt.zip")
    val ac = ConfigProject.loadLocalAnalysis(project).get
    val mapper = PathMapper.fromConfigProject(project)
    ConfigProject.saveLocalAnalysisToPortable(
      project, pathTo, ac, mapper
    )
  }
  test("restoreLocalAnalysisFromPortable",ManualTag) {
    val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
    val pathFrom = Paths.get("workspace/save/rbpcommander.prt.zip")
    val pathTo = Paths.get("workspace/save/rbpcommander.bin.zip")
    val ac = ConfigProject.loadRemoteAnalysis(project,pathFrom)
    val mapper = PathMapper.fromConfigProject(project)
    ConfigProject.restoreLocalAnalysisFromPortable(
      project, pathTo, ac.get, mapper
    )
    TestAnalysis.traceReadAnalysis(pathTo)
  }
}
