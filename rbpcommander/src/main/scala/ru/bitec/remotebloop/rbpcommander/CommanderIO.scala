package ru.bitec.remotebloop.rbpcommander

import java.io.File
import java.nio.file.Path

import sbt.internal.inc.Stamper
import xsbti.compile.analysis.Stamp

object CommanderIO {
  val isFileSystemCaseSensitive = !new File("a").equals(new File("A"))
  private lazy val _emptyStamp = stampFromString("absent")
   def fileHashFromPath( path: Path): Stamp = {
     //import sbt.io.{Hash=> IOHash}
     //IOHash.toHex(IOHash(path.toFile))
     Stamper.forHash(path.toFile)
   }
   def lastModifiedFromPath(path: Path ): Stamp = {
     Stamper.forLastModified(path.toFile)
   }
   def emptyStamp(): Stamp = {
     _emptyStamp
   }

  def stampFromString(stampString: String):Stamp = {
    import sbt.internal.inc.Stamp
    Stamp.fromString(stampString)
  }
  def lastModifiedFromLong(lastModified: Long):Stamp = {
    stampFromString(s"lastModified($lastModified)")
  }
}
