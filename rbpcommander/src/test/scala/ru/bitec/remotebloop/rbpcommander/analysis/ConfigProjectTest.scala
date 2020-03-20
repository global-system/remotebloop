package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Paths

class ConfigProjectTest extends org.scalatest.FunSuite {
  val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
  test("getPathMapper") {
    val resutl = PathMapper.fromConfigProject(project)
    assert(resutl.inRootDirMaps.mapByKey.nonEmpty)
  }
  test("saveLocalAnalysisToPortable") {
    val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
    val pathTo = Paths.get("workspace\\save\\rbpcommander.prt.zip")
    val ac = ConfigProject.loadLocalAnalysis(project).get
    val mapper = PathMapper.fromConfigProject(project)
    /*val fileMetaMaps = new FileMetaMaps(
      rootFiles.rootFiles.map{rootFile =>
        FileMeta(path = rootFile.path,hash = Option(AnalysisIO.getFileHash(rootFile.path)))
      }
    )*/
    ConfigProject.saveLocalAnalysisToPortable(
      project, pathTo, ac, mapper
    )
  }
  test("restoreLocalAnalysisFromPortable") {
    val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
    val pathFrom = Paths.get("workspace\\save\\rbpcommander.prt.zip")
    val pathTo = Paths.get("workspace\\save\\rbpcommander.bin.zip")
    val ac = TestAnalysis.traceReadAnalysis(pathFrom)
    val mapper = PathMapper.fromConfigProject(project)
    /*val fileMetaMaps = new FileMetaMaps(
      rootFiles.rootFiles.map{rootFile =>
        FileMeta(path = rootFile.path,hash = Option(AnalysisIO.getFileHash(rootFile.path)))
      }
    )*/
    ConfigProject.restoreLocalAnalysisFromPortable(
      project, pathTo, ac, mapper
    )
    TestAnalysis.traceReadAnalysis(pathTo)
  }
}
