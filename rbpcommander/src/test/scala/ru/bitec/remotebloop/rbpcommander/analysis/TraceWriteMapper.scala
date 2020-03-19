package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File

import xsbti.compile.MiniSetup
import xsbti.compile.analysis.{Stamp, WriteMapper}

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