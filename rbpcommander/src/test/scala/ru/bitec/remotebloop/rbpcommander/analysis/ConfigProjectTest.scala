package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Paths

class ConfigProjectTest extends org.scalatest.FunSuite {
   val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
   test("getInRootDirs"){
      val resutl = ConfigProject.getInRootDirs(project)
      assert(resutl.mapByKey.nonEmpty)
   }
  test("getRootFiles"){
    val resutl = ConfigProject.getRootFiles(project)
    assert(resutl.mapByKey.nonEmpty)
  }
  test("saveLocalAnalysisToPortable"){
    val project = ConfigProject.loadProject(Paths.get(".bloop/rbpcommander.json"))
    val pathTo = Paths.get("workspace\\save\\rbpcommander.bin.zip")
    val ac = ConfigProject.loadLocalAnalysis(project).get
    val rootFiles = ConfigProject.getRootFiles(project)
    val inRootDirs = ConfigProject.getInRootDirs(project)
    val outRootDirs = ConfigProject.getOutRootDirs(project,ac)
    val fileMetaMaps = new FileMetaMaps(
      rootFiles.rootFiles.map{rootFile =>
        FileMeta(path = rootFile.path,hash = Option(AnalysisIO.getFileHash(rootFile.path)))
      }
    )
    ConfigProject.saveLocalAnalysisToPortable(pathTo,ac,inRootDirs,outRootDirs,rootFiles,fileMetaMaps)
  }
}
