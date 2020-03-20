package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File

import xsbti.compile.MiniSetup
import xsbti.compile.analysis.{Stamp, WriteMapper}


class RestoreWriteMapper(pathMapper:PathMapper) extends WriteMapper{
  override def mapSourceFile(sourceFile: File): File = {
    pathMapper.fromPortableFile(sourceFile)
  }
  override def mapBinaryFile(binaryFile: File): File = {
    pathMapper.fromPortableFile(binaryFile)
  }
  override def mapProductFile(productFile: File): File = {
    pathMapper.fromPortableFile(productFile)
  }

  override def mapOutputDir(outputDir: File): File = {
    pathMapper.fromPortableFile(outputDir)
  }

  override def mapSourceDir(sourceDir: File): File = {
    pathMapper.fromPortableFile(sourceDir)
  }

  override def mapClasspathEntry(classpathEntry: File): File = {
    pathMapper.fromPortableFile(classpathEntry)
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
