package ru.bitec.remotebloop.rbpcommander.analysis

import java.io.File
import java.nio.file.{FileSystem, Path, Paths}

import bloop.config.Config.{JvmConfig, Project}
import ru.bitec.remotebloop.rbpcommander.CommanderIO
import xsbti.compile.analysis.Stamp

import scala.collection.mutable.ArrayBuffer


case class RootDir(key:String, path: Path){
  val pathString = if (CommanderIO.isFileSystemCaseSensitive) path.toString else path.toString.toLowerCase()
}

case class RootDirMaps(rootDirs:List[RootDir]){
  val mapByKey: Map[String,RootDir] =  rootDirs.map{d => d.key-> d}.toMap
  val mapByPath: Map[String,RootDir] = rootDirs.map{d => d.pathString-> d}.toMap
  val pathArray:Array[String] = mapByPath.keys.toArray.sortBy(-_.length)

  for(rd<-rootDirs if rd.key.contains('.')){
    throw  new RuntimeException("Root dir key cannot contain point")
  }

  def relativizeByPath(path:Path): Option[RootDir] ={
    var curRootDir: String = null
    var i =0
    val unSavePathString = path.toAbsolutePath.toString
    val pathString = if (CommanderIO.isFileSystemCaseSensitive) unSavePathString else unSavePathString.toLowerCase()
    val size = pathArray.length
    while(i<size && curRootDir == null){
      if (pathString.startsWith(pathArray(i))){
        //the first match is correct because array is sorted by length.
        curRootDir = pathArray(i)
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
      val file = new File(s"${rootDir.key}./$postfix")
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

case class PathMapper(fileSystem: FileSystem, inRootDirMaps: RootDirMaps, outRootDirMaps: RootDirMaps, rootFileMaps: RootFileMaps){
  val sep = fileSystem.getSeparator
  val oldSep = if (sep.eq("/")) "\\" else "/"
  def fromPortableFile(file:File):File = {
    def split(string: String): (String,String) = {

      val i = string.indexOf('.')
      if (i<0){
        throw new RuntimeException("There is an error in portable path format")
      }
      val s1 = string.substring(0,i)
      val s2 = string.substring(i+1)
      //Path can transparently change slash so we cannot guarantee  single character
      (s1,s2.replace(oldSep,sep))
    }
    val fileString = file.toString
    val i1 = fileString.indexOf('_')
    if (i1<0){
      throw new RuntimeException("There is an error in portable path format")
    }
    val tp = fileString.substring(0,i1)
    val result = tp match {
      case "ird" =>
        val t2 = split(fileString)
        inRootDirMaps.mapByKey(t2._1).path.resolve(t2._2.stripPrefix(sep))
      case "ord" =>
        val t2 = split(fileString)
        outRootDirMaps.mapByKey(t2._1).path.resolve(t2._2.stripPrefix(sep))
      case "rf"  =>
        rootFileMaps.mapByKey(fileString).path
    }
    result.toFile
  }
}

object PathMapper{
  private def getInRootDirs(configProject: Project): RootDirMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootDir]
    for (platform <- configProject.platform;
         jvmConfig <- Option(platform.config).collect { case c: JvmConfig => c };
         home <- jvmConfig.home
         ) {
      arrayBuffer.append(RootDir("ird_jvm_home", home.toAbsolutePath))
    }
    arrayBuffer.append(RootDir("ird_project_dir", configProject.directory.toAbsolutePath))
    arrayBuffer.append(RootDir("ird_maven",Paths.get( "C:\\Users\\AMatveev\\AppData\\Local\\Coursier\\cache\\v1\\https\\repo1.maven.org\\maven2")))
    RootDirMaps(arrayBuffer.toList)
  }
  private def getOutRootDirs(configProject: Project): RootDirMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootDir]
    arrayBuffer.append(RootDir("ord_root_out", configProject.out.toAbsolutePath))
    /*    val comps = analysisContents.getAnalysis.readCompilations().getAllCompilations
          comps.foreach{c =>
          c.getOutput.getSingleOutput match{
            case o if o.isPresent =>
              arrayBuffer.append(RootDir(s"co_last_compilation", o.get().toPath.toAbsolutePath))
            case _ =>
          }
        }*/
    /*      analysisContents.getMiniSetup.output().getSingleOutput match{
          case o if o.isPresent =>
            arrayBuffer.append(RootDir("ord_single_out", o.get().toPath.toAbsolutePath))
          case _ =>
        }*/
    RootDirMaps(arrayBuffer.toList)
  }
  private def getRootFiles(configProject: Project): RootFileMaps = {
    val arrayBuffer = ArrayBuffer.empty[RootFile]
    for (resolution <- configProject.resolution;
         module <- resolution.modules;
         artifact <- module.artifacts if artifact.classifier.isEmpty
         ) {
      arrayBuffer.append(RootFile(s"rf_${module.organization}_${artifact.name}".toLowerCase, artifact.path.toAbsolutePath))
    }
    RootFileMaps(arrayBuffer.toList)
  }

  def fromConfigProject(configProject: Project): PathMapper = {
    val fs = configProject.directory.getFileSystem
    PathMapper(fs,getInRootDirs(configProject),getOutRootDirs(configProject),getRootFiles(configProject))
  }
}
