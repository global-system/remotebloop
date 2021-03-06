package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.{Files, Path, StandardCopyOption}

import bloop.config.Config.Project
import ru.bitec.remotebloop.rbpcommander.{FilePath, MetaCache}
import sbt.internal.inc.{Analysis, FileAnalysisStore}
import xsbti.compile.AnalysisContents
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers, WriteMapper}


object ConfigProject {


  def loadProject(filePath: Path): Project = {
    import bloop.config.read
    val config = read(filePath)
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
  def loadRemoteAnalysis(project: Project, fileFromPath: Path): Option[AnalysisContents] = {
    if (Files.exists(fileFromPath)){
      val out = project.out
      Files.createDirectories(out)
      val tempFile = out.resolve(s"tmpInAnalisys-${project.name}.zip")
      try{
        Files.copy(fileFromPath,tempFile,StandardCopyOption.REPLACE_EXISTING)
        val readMapper = ReadMapper.getEmptyMapper
        val writeMapper = WriteMapper.getEmptyMapper
        val mappers = new ReadWriteMappers(readMapper, writeMapper)
        val remoteStore = FileAnalysisStore.binary(tempFile.toFile, mappers)
        val analysis = remoteStore.get().get()
        Some(analysis)
      }finally{
        if (Files.exists(tempFile)){
          Files.delete(tempFile)
        }
      }
    } else {
      None
    }
  }

  def saveLocalAnalysisToPortable(project: Project,
                                  fileToPath: Path,
                                  analysisContents: AnalysisContents,
                                  pathMapper: PathMapper
                                 ): Unit = {
    val out = project.out
    val tempFile = out.resolve(s"tmpOutAnalisys-${project.name}.zip")
    val readMapper = ReadMapper.getEmptyMapper
    val writeMapper = new SaveWriteMapper(pathMapper)
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(tempFile.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
    Files.move(tempFile,fileToPath,StandardCopyOption.REPLACE_EXISTING)
  }

  def restoreLocalAnalysisFromPortable(project: Project,
                                       fileToPath: Path,
                                       analysisContents: AnalysisContents,
                                       pathMapper: PathMapper,
                                       metaCacheFiles: List[FilePath]
                                      ): Unit = {
    val readMapper = ReadMapper.getEmptyMapper
    val writeMapper = new RestoreWriteMapper(pathMapper,MetaCache(metaCacheFiles))
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(fileToPath.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }
}
