package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File

import ru.bitec.remotebloop.rbpcommander.CommanderIO
import sbt.internal.inc.Stamper
import xsbti.compile.MiniSetup
import xsbti.compile.analysis.{ReadMapper, Stamp}

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
    val stam1 = Stamper.forLastModified(file)
    val stamp2 = CommanderIO.lastModifiedFromPath(file.toPath)
    val stamp3 = Stamper.forLastModified
    println(s"mapBinaryStamp:$file,$binaryStamp")
    binaryStamp
  }

  override  def mapSourceStamp(file: File, sourceStamp: Stamp): Stamp = {
   // val hash1 = sourceStamp.getHash.get()
   // val hash2 = Stamper.forHash(file)
   // val hash3 = AnalysisIO.getFileHash(file.toPath)
   // println(s"mapSourceStamp:$file,$sourceStamp")
    sourceStamp
  }

  override  def mapProductStamp(file: File, productStamp: Stamp): Stamp = {
    println(s"mapProductStamp:$file,$productStamp")
    //val s1 = productStamp
    //val s2 = CommanderIO.lastModifiedFromLong(file.lastModified())
    productStamp
  }

  override  def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = {
    println(s"mapMiniSetup:$miniSetup")
    miniSetup
  }
};
