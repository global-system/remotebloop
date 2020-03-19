package ru.bitec.remotebloop.rbpcommander.analysis

import java.nio.file.Path

import sbt.internal.inc.{Analysis, FileAnalysisStore}
import xsbti.compile.AnalysisContents
import xsbti.compile.analysis.{ReadWriteMappers, WriteMapper}


object TestAnalysis {
   def traceReadAnalysis(path: Path): AnalysisContents = {
     val readMapper = new TraceReadMapper()
     val writeMapper = WriteMapper.getEmptyMapper
     val mappers = new ReadWriteMappers(readMapper, writeMapper)
     val remoteStore = FileAnalysisStore.binary(path.toFile, mappers)
     val analysis = remoteStore.get().get()
     analysis
   }
  def traceWritAnalysis(pathTo: Path, analysisContents: AnalysisContents): Unit = {
    val readMapper = new TraceReadMapper()
    val writeMapper = new TraceWriteMapper()
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(pathTo.toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }
}
