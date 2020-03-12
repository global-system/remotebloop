package ru.bitec.remotebloop.rbpcommander

import java.io.File
import java.nio.file.Paths

import sbt.internal.inc.{Analysis, FileAnalysisStore}
import xsbti.compile.{AnalysisContents, AnalysisStore, MiniSetup}
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers, Stamp, WriteMapper}

object FileAnalysisStoreTest {
  val toFile = Paths.get("workspace\\inc_compile_2.12exp.zip").toAbsolutePath.toFile
  val fromFile = Paths.get("rbpcommander\\target\\streams\\compile\\bloopAnalysisOut\\_global\\streams\\inc_compile_2.12.zip").toAbsolutePath.toFile

  def getMachineIndependentStore(): AnalysisStore = {
    val readMapper = new TraceReadMapper()
    val writeMapper = WriteMapper.getEmptyMapper
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val remoteStore = FileAnalysisStore.binary(fromFile, mappers)
    remoteStore
  }

  def exportAnalisisStore(analysisContents: AnalysisContents): Unit = {
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val remoteStore = getMachineIndependentStore()
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    remoteStore.set(contents)
  }

  def saveAnalisisStore(analysisContents: AnalysisContents): Unit = {
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(toFile)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }

  def main(args: Array[String]): Unit = {
    val remoteStore = getMachineIndependentStore()
    val acOption = remoteStore.get()
    saveAnalisisStore(acOption.get())
  }
}

class TraceReadMapper extends ReadMapper {
  override def mapSourceFile(sourceFile: File): File = {
    println(s"mapSourceFile:$sourceFile")
    sourceFile
  }

  override def mapBinaryFile(binaryFile: File): File = {
    println(s"mapBinaryFile:$binaryFile")
    binaryFile
  }

  override def mapProductFile(productFile: File): File = {
    println(s"mapProductFile:$productFile")
    productFile
  }

  override  def mapOutputDir(outputDir: File): File = {
    println(s"mapOutputDir:$outputDir")
    outputDir
  }

  override  def mapSourceDir(sourceDir: File): File = {
    println(s"mapSourceDir:$sourceDir")
    sourceDir
  }

  override  def mapClasspathEntry(classpathEntry: File): File = {
    println(s"mapClasspathEntry:$classpathEntry")
    classpathEntry
  }

  override  def mapJavacOption(javacOption: String): String = {
    println(s"mapJavacOption:$javacOption")
    javacOption
  }

  override  def mapScalacOption(scalacOption: String): String = {
    println(s"mapScalacOption:$scalacOption")
    scalacOption
  }

  override  def mapBinaryStamp(file: File, binaryStamp: Stamp): Stamp = {
    println(s"mapBinaryStamp:$file")
    binaryStamp
  }

  override  def mapSourceStamp(file: File, sourceStamp: Stamp): Stamp = {
    println(s"mapSourceStamp:$file")
    sourceStamp
  }

  override  def mapProductStamp(file: File, productStamp: Stamp): Stamp = {
    println(s"mapProductStamp:$file")
    productStamp
  }

  override  def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = {
    println(s"mapMiniSetup:$miniSetup")
    miniSetup
  }
};