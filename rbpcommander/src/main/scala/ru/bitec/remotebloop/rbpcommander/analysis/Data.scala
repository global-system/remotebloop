package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File
import java.nio.file.Path

import xsbti.compile.analysis.Stamp


case class RootDir(key:String, path: Path){
  val pathString = if (AnalysisIO.isFileSystemCaseSensitive) path.toString else path.toString.toLowerCase()
}

case class RootDirMaps(rootDirs:List[RootDir]){
  val mapByKey: Map[String,RootDir] =  rootDirs.map{d => d.key-> d}.toMap
  val mapByPath: Map[String,RootDir] = rootDirs.map{d => d.pathString-> d}.toMap
  val  pathArray:Array[String] = mapByPath.keys.toArray
  def relativizeByPath(path:Path): Option[RootDir] ={
    var curRootDir: String = null
    var i =0
    val unSavePathString = path.toAbsolutePath.toString
    val pathString = if (AnalysisIO.isFileSystemCaseSensitive) unSavePathString else unSavePathString.toLowerCase()
    val size = pathArray.length
    while(i<size){
      if (curRootDir == null || curRootDir.length<pathArray(i).length){
        if (pathString.startsWith(pathArray(i))){
          curRootDir = pathArray(i)
        }
      }
      i=i+1
    }
    if (curRootDir == null){
      None
    } else {
      mapByPath.get(curRootDir)
    }
  }
  def toPortableFile(file: File): Option[File] = {
    val sourceFile = file
    relativizeByPath(file.toPath).flatMap{ rootDir =>
      val sourceString = sourceFile.toString
      val postfix = sourceString.substring(rootDir.pathString.length)
      val file = new File(s"${rootDir.key}/$postfix")
      Some(file)
    }
  }
}

case class RootFile(key:String, path: Path)
case class RootFileMaps(rootFiles:List[RootFile]){
  val mapByKey: Map[String,RootFile]= rootFiles.map{d => d.key-> d}.toMap
  val mapByPath: Map[String,RootFile]=  rootFiles.map{d => d.path.toString-> d}.toMap
  def toPortableFile(file: File): Option[File] = {
    mapByPath.get(file.toString).flatMap{ rootFile =>
      val file = new File(rootFile.key)
      Some(file)
    }
  }
}

case class FileMeta(path: Path, lastModified: Option[Stamp] = None, hash: Option[Stamp]= None)
class FileMetaMaps(fileMetas: List[FileMeta]){
  val mapByPath: Map[String,FileMeta]=  fileMetas.map{d => d.path.toString-> d}.toMap
}
