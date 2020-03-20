package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.{Files, Path, StandardCopyOption}

import bloop.config.Config.Project
import sbt.internal.inc.{Analysis, FileAnalysisStore}
import xsbti.compile.AnalysisContents
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers}


object ConfigProject {


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

  def saveLocalAnalysisToPortable( project: Project,
                                   pathTo: Path,
                                   analysisContents: AnalysisContents,
                                   pathMapper: PathMapper
                                 ): Unit = {
    val out = project.out
    val tempFile = out.resolve("tmpOutAnalisys.zip")
    val readMapper = ReadMapper.getEmptyMapper
    val writeMapper = new SaveWriteMapper(pathMapper)
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(tempFile.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
    Files.move(tempFile,pathTo,StandardCopyOption.REPLACE_EXISTING)
  }

  def restoreLocalAnalysisFromPortable( project: Project,
                                        pathTo: Path,
                                        analysisContents: AnalysisContents,
                                        pathMapper: PathMapper
                                      ): Unit = {
    val readMapper = ReadMapper.getEmptyMapper
    val writeMapper = new RestoreWriteMapper(pathMapper)
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(pathTo.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }
}
