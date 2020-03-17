package ru.bitec.remotebloop.rbpcommander

import java.io.File
import java.lang
import java.nio.file.Paths
import java.util.Optional

import sbt.io.{IO, Hash => IOHash}
import sbt.internal.inc.{Analysis, FileAnalysisStore, Hash}
import sbt.ru.bitec.remotebloop.HashUtiles
import xsbti.compile.{AnalysisContents, AnalysisStore, MiniSetup}
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers, Stamp, WriteMapper}

object FileAnalysisStoreExample {
  val toFile = Paths.get("workspace\\inc_compile_2.12exp.zip").toAbsolutePath.toFile
  val fromFile = Paths.get("rbpcommander\\target\\streams\\compile\\bloopAnalysisOut\\_global\\streams\\inc_compile_2.12.zip").toAbsolutePath.toFile

  def getMachineIndependentStore(file: File): AnalysisStore = {
    val readMapper = new TraceReadMapper()
    val writeMapper = WriteMapper.getEmptyMapper
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val remoteStore = FileAnalysisStore.binary(file, mappers)
    remoteStore
  }


  def saveAnalisisStore(analysisContents: AnalysisContents): Unit = {
    val readMapper = new TraceReadMapper()
    val writeMapper = new TraceWriteMapper()
    val mappers = new ReadWriteMappers(readMapper, writeMapper)
    val currentAnalysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    val currentSetup = analysisContents.getMiniSetup
    val store = FileAnalysisStore.binary(toFile,mappers)
    val contents = AnalysisContents.create(currentAnalysis, currentSetup)
    store.set(contents)
  }

  def main(args: Array[String]): Unit = {
    val remoteStore = getMachineIndependentStore(fromFile)
    val acOption = remoteStore.get()
    saveAnalisisStore(acOption.get())
    println("readOut")
    val remoteStore2 = getMachineIndependentStore(toFile)
    val acOption3 = remoteStore2.get()
  }
}

class TraceReadMapper extends ReadMapper {
  override def mapSourceFile(sourceFile: File): File = {
   // println(s"mapSourceFile:$sourceFile")
    sourceFile
  }

  override def mapBinaryFile(binaryFile: File): File = {
   // println(s"mapBinaryFile:$binaryFile")
    binaryFile
  }

  override def mapProductFile(productFile: File): File = {
    //println(s"mapProductFile:$productFile")
    productFile
  }

  override  def mapOutputDir(outputDir: File): File = {
    //println(s"mapOutputDir:$outputDir")
    outputDir
  }

  override  def mapSourceDir(sourceDir: File): File = {
    //println(s"mapSourceDir:$sourceDir")
    sourceDir
  }

  override  def mapClasspathEntry(classpathEntry: File): File = {
    //println(s"mapClasspathEntry:$classpathEntry")
    classpathEntry
  }

  override  def mapJavacOption(javacOption: String): String = {
    println(s"mapJavacOption:$javacOption")
    javacOption
  }

  override  def mapScalacOption(scalacOption: String): String = {
    //println(s"mapScalacOption:$scalacOption")
    scalacOption
  }

  override  def mapBinaryStamp(file: File, binaryStamp: Stamp): Stamp = {
    println(s"mapBinaryStamp:$file,$binaryStamp,${binaryStamp.getClass}")
    HashUtiles.fromString("hash(3234)").get
  }

  override  def mapSourceStamp(file: File, sourceStamp: Stamp): Stamp = {
    //println(s"mapSourceStamp:$file,$sourceStamp")
    sourceStamp
  }

  override  def mapProductStamp(file: File, productStamp: Stamp): Stamp = {
    //println(s"mapProductStamp:$file,$productStamp")
    val s1 = IOHash.fromHex( IOHash.toHex(IOHash(file)))
    val s2 = IOHash(file)
    productStamp
  }

  override  def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = {
    //println(s"mapMiniSetup:$miniSetup")
    miniSetup
  }
};

class TraceWriteMapper extends WriteMapper{
  override def mapSourceFile(sourceFile: File): File = {
    sourceFile
  }

  override def mapBinaryFile(binaryFile: File): File = {
    binaryFile
  }

  override def mapProductFile(productFile: File): File = {
    productFile
  }

  override def mapOutputDir(outputDir: File): File = {
    outputDir
  }

  override def mapSourceDir(sourceDir: File): File = {
    sourceDir
  }

  override def mapClasspathEntry(classpathEntry: File): File = {
    classpathEntry
  }

  override def mapJavacOption(javacOption: String): String = {
    javacOption
  }

  override def mapScalacOption(scalacOption: String): String = {
    scalacOption
  }

  override def mapBinaryStamp(file: File, binaryStamp: Stamp): Stamp = {
    binaryStamp
  }

  override def mapSourceStamp(file: File, sourceStamp: Stamp): Stamp = {
    sourceStamp
  }

  override def mapProductStamp(file: File, productStamp: Stamp): Stamp = {
    productStamp
  }

  override def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = {
    miniSetup
  }
}