package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File
import java.nio.file.Path

import sbt.internal.inc.Stamper
import xsbti.compile.analysis.Stamp


object AnalysisIO {
  val isFileSystemCaseSensitive = !new File("a").equals(new File("A"))
   private lazy val lastModified: Stamp = {
     //There are no public constructor
     var file = new File(".bloop\\lastmodifieddummyfile")
     var i = 0
     while(file.exists()){
       file = new File(s".bloop\\${System.nanoTime()}")
       i=i+1
       if (i>100){
         throw  new RuntimeException("Zero LastMofified value cannot be generated")
       }
     }
     Stamper.forLastModified(file)
   }
   def getFileHash( path: Path): Stamp = {
     //import sbt.io.{Hash=> IOHash}
     //IOHash.toHex(IOHash(path.toFile))
     Stamper.forHash(path.toFile)
   }
   def getFileLastModified(path: Path ): Stamp = {
     Stamper.forLastModified(path.toFile)
   }
   def getZeroLastModified(): Stamp = {
     lastModified
   }
}
