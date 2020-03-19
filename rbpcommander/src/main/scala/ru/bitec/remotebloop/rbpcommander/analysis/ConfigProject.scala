package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Path

import bloop.config.Config.{JvmConfig, Project}
import sbt.internal.inc.{Analysis, FileAnalysisStore}
import xsbti.compile.AnalysisContents
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers}

import scala.collection.mutable.ArrayBuffer

object ConfigProject {
  def getInRootDirs(configProject: Project): RootDirMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootDir]
    for (platform <- configProject.platform;
         jvmConfig <- Option(platform.config).collect { case c: JvmConfig => c };
         home <- jvmConfig.home
         ) {
      arrayBuffer.append(RootDir("rd_jvm_home", home.toAbsolutePath))
    }
    arrayBuffer.append(RootDir("rd_project_dir", configProject.directory.toAbsolutePath))
    new RootDirMaps(arrayBuffer.toList)
  }
  def getOutRootDirs(configProject: Project,analysisContents: AnalysisContents): RootDirMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootDir]
    arrayBuffer.append(RootDir("ou_root_out", configProject.out.toAbsolutePath))
/*    val comps = analysisContents.getAnalysis.readCompilations().getAllCompilations
      comps.foreach{c =>
      c.getOutput.getSingleOutput match{
        case o if o.isPresent =>
          arrayBuffer.append(RootDir(s"co_last_compilation", o.get().toPath.toAbsolutePath))
        case _ =>
      }
    }*/
      analysisContents.getMiniSetup.output().getSingleOutput match{
      case o if o.isPresent =>
        arrayBuffer.append(RootDir("ou_single_out", o.get().toPath.toAbsolutePath))
      case _ =>
    }
    RootDirMaps(arrayBuffer.toList)
  }
  def getRootFiles(configProject: Project): RootFileMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootFile]
    for (resolution <- configProject.resolution;
         module <- resolution.modules;
         artifact <- module.artifacts if artifact.classifier.isEmpty
         ) {
      arrayBuffer.append(RootFile(s"rf_${module.organization}_${artifact.name}".toLowerCase, artifact.path.toAbsolutePath))
    }
    new RootFileMaps(arrayBuffer.toList)
  }

  def loadProject(path: Path): Project = {
    import bloop.config.read
    val config = read(path)
    val Right(result) = config
    result.project
  }

  def loadLocalAnalysis(configProject: Project): Option[AnalysisContents] = {
    val analysis = (for (s <- configProject.scala; a <- s.analysis) yield {
      val file = a.toFile
      if (file.exists()) {
        val remoteStore = FileAnalysisStore.binary(file)
        remoteStore.get() match {
          case v if v.isPresent =>
            Some(v.get())
          case _ => None
        }
      } else {
        None
      }
    }).flatten
    analysis
  }

  def saveLocalAnalysisToPortable( pathTo: Path,
                                   analysisContents: AnalysisContents,
                                   rootInDirMaps: RootDirMaps,
                                   rootOutDirMaps: RootDirMaps,
                                   rootFileMaps: RootFileMaps,
                                   fileMetaMaps: FileMetaMaps
                                 ): Unit = {
    val readMapper = ReadMapper.getEmptyMapper
    val writeMapper = new SaveWriteMapper(rootInDirMaps,rootOutDirMaps,rootFileMaps,fileMetaMaps)
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(pathTo.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }
}
