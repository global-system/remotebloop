package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File

import xsbti.compile.{FileHash, MiniOptions, MiniSetup}
import xsbti.compile.analysis.{Stamp, WriteMapper}

class SaveWriteMapper(inRootDirMaps: RootDirMaps, outRootDirMaps: RootDirMaps, rootFileMaps: RootFileMaps, fileMetaMaps: FileMetaMaps) extends WriteMapper{
  override def mapSourceFile(sourceFile: File): File = {
    val result=inRootDirMaps.toPortableFile(sourceFile)
    result.get
  }

  override def mapBinaryFile(binaryFile: File): File = {
    val rf = rootFileMaps.toPortableFile(binaryFile)
    rf match {
      case Some(f) => f
      case None =>
        val rd = inRootDirMaps.toPortableFile(binaryFile)
        rd.get
    }
  }

  override def mapProductFile(productFile: File): File = {
    val sf=outRootDirMaps.toPortableFile(productFile)
    sf.get
  }

  override def mapOutputDir(outputDir: File): File = {
    val sf=outRootDirMaps.toPortableFile(outputDir)
    sf.get
  }

  override def mapSourceDir(sourceDir: File): File = {
    val sf=inRootDirMaps.toPortableFile(sourceDir)
    sf.get
  }

  override def mapClasspathEntry(classpathEntry: File): File = {
    val result = rootFileMaps.toPortableFile(classpathEntry)
    result.get
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
    /*val cph = miniSetup.options().classpathHash()
    val newCph = cph.map{h =>
      val pf = rootFileMaps.toPortableFile(h.file())
      FileHash.of(pf.get,h.hash())
    }
    val newOptions = MiniOptions.of(
      newCph,
      miniSetup.options().scalacOptions(),
      miniSetup.options().javacOptions()
    )
    val newMiniSetup = MiniSetup.of(
      miniSetup.output(),
      newOptions,
      miniSetup.compilerVersion(),
      miniSetup.order(),
      miniSetup.storeApis(),
      miniSetup.extra()
    )
    newMiniSetup*/
    miniSetup
  }
}
