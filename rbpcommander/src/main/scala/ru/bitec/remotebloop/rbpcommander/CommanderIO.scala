package ru.bitec.remotebloop.rbpcommander

import java.io.File
import java.nio.file.{Files, Path, Paths}

import play.api.libs.json.{JsObject, Json}
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
  def sharedDirs():List[(String,Path)]  = {
    val jv = Json.parse(Files.readAllBytes(Paths.get("config/shareddirs.json")))
    jv.as[JsObject].value.toList.map(v => v._1 -> Paths.get(v._2.as[String]))
  }

}
