package ru.bitec.remotebloop.rbpcommander

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor, StandardCopyOption}

import monix.eval.Task

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}
trait ScanPath{
  val level:Int
  val key: String
  def keyToPathString(sep: String):String = {
    key.replace("/",sep)
  }
}

case class DirPath(key: String,path: Path,level: Int) extends ScanPath
case class FilePath(key: String,path: Path,level: Int,lastModified: Long,hash:String) extends ScanPath
case class ScanFiles(root:Path, dirs: Map[String,DirPath],files: Map[String,FilePath])
case class MetaCache(metaCacheFiles: List[FilePath]){
  val mapByPath:Map[Path,FilePath] = metaCacheFiles.map(f => f.path -> f).toMap
}
case class SyncAction(
                       needDeleteTargetFiles: List[FilePath],
                       needDeleteTargetDirs: List[DirPath],
                       needIgnoreTargetFiles: List[FilePath],
                       needReplaceSourceFiles: List[FilePath],
                       needCreateSourceDirs: List[DirPath]
                     )

class ScanFileVisitor(val rootPath: Path) extends SimpleFileVisitor[Path]{
  var level:Int = 0
  private val dirListBuffer = ArrayBuffer.empty[DirPath]
  private val fileListBuffer = ArrayBuffer.empty[FilePath]
  override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
    val key = CommanderIO.keyByPath(rootPath.relativize(dir))
    dirListBuffer.append(DirPath(key,dir,level))
    level = level +1;
    super.preVisitDirectory(dir, attrs)
  }

  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    val key = CommanderIO.keyByPath(rootPath.relativize(file))
    fileListBuffer.append(FilePath(key,file,level,attrs.lastModifiedTime().toMillis,""))
    super.visitFile(file, attrs)
  }

  override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
    level = level  - 1;
    super.postVisitDirectory(dir, exc)
  }
  def build(): ScanFiles = {
    ScanFiles(
      rootPath,
      dirListBuffer.map{i => (i.key,i)}.toMap,
      fileListBuffer.map{i => (i.key,i)}.toMap
    )
  }
}
object FileSyncTasks {
  import TryTask.Implicits._
  def scanPath(sourceDir: Path):Task[Try[ScanFiles]] = TryTask{
   // val start = System.nanoTime()
    val visitor = new ScanFileVisitor(sourceDir)
    if (Files.exists(sourceDir)){
      Files.walkFileTree(sourceDir, visitor)
    }
    val result = visitor.build()
    //val end = System.nanoTime()-start
    //println(s"scan ${sourcePath.getFileSystem},$sourcePath \r\n in $end nanos")
    result
  }
  def syncScanFiles(source: ScanFiles,target: ScanFiles,isIncremental: Boolean=false): Task[Try[List[FilePath]]] = TryTask{
    val needDeleteTargetFiles = ArrayBuffer.empty[FilePath]
    target.files.foreach{case (key,file) =>
      if (!source.files.isDefinedAt(key)) {
        needDeleteTargetFiles.append(file)
      }
    }
    val needDeleteTargetDirs = ArrayBuffer.empty[DirPath]
    target.dirs.foreach{case (key,dir) =>
      if (!source.dirs.isDefinedAt(key)) {
        needDeleteTargetDirs.append(dir)
      }
    }
    val needReplaceSourceFiles = ArrayBuffer.empty[FilePath]
    val needIgnoreTargetFiles = ArrayBuffer.empty[FilePath]
    source.files.foreach{case (targetKey,sourceFile) =>
      target.files.get(targetKey) match {
        case Some(targetFile) if isIncremental && targetFile.hash == sourceFile.hash =>
          needIgnoreTargetFiles.append(targetFile)
        case _ =>
          needReplaceSourceFiles.append(sourceFile)
      }
    }
    val needCreateSourceDirs = ArrayBuffer.empty[DirPath]
    source.dirs.foreach{case (sourceKey,sourceDir) =>
      target.dirs.get(sourceKey) match {
        case Some(_)  =>
        case _ =>
          needCreateSourceDirs.append(sourceDir)
      }
    }
    SyncAction(
      needDeleteTargetFiles = needDeleteTargetFiles.toList,
      needDeleteTargetDirs= needDeleteTargetDirs.toList,
      needIgnoreTargetFiles = needIgnoreTargetFiles.toList,
      needReplaceSourceFiles = needReplaceSourceFiles.toList,
      needCreateSourceDirs = needCreateSourceDirs.toList
    )
  }.tryFlatMap{syncAction =>
    val sep = target.root.getFileSystem.getSeparator
    val deleteTargetFilesTask = TryTask{
      syncAction.needDeleteTargetFiles.foreach{ targetFile =>
        Files.delete(targetFile.path)
      }
      Nil
    }
    val replaceTargetFilesTask = TryTask{
      val buffer = ArrayBuffer.empty[FilePath]
      syncAction.needReplaceSourceFiles.foreach{ targetFile =>
        val targetPath = target.root.resolve(targetFile.keyToPathString(sep))
        val sourcePath = targetFile.path
        Files.copy(sourcePath,targetPath,StandardCopyOption.REPLACE_EXISTING)
        buffer.append(
          targetFile.copy(path=targetPath,lastModified = Files.getLastModifiedTime(targetPath).toMillis)
        )
      }
      buffer ++ syncAction.needIgnoreTargetFiles
    }
    val deleteTargetDirTask = TryTask{
      syncAction.needDeleteTargetDirs.sortBy(-_.level).foreach{ targetDir =>
        val dirPath = target.root.resolve(targetDir.keyToPathString(sep))
        Files.delete(dirPath)
      }
      Nil
    }
    val createTargetDirTask = TryTask{
      syncAction.needCreateSourceDirs.sortBy(_.level).foreach{ dir =>
        val dirPath = target.root.resolve(dir.keyToPathString(sep))
        Files.createDirectory(dirPath)
      }
      Nil
    }
    val drTargetFilesTask = Task.gather(deleteTargetFilesTask::replaceTargetFilesTask::Nil).groupByTry().tryMap{ list =>
      list.flatten
    }
    Task.sequence(createTargetDirTask::drTargetFilesTask::deleteTargetDirTask::Nil).groupByTry().tryMap{list =>
      list.flatten
    }
  }
  def sync(sourceDir: Path, targetDir: Path): Task[Try[List[FilePath]]] = {
    val sourceScanTask = scanPath(sourceDir)
    val targeScanTask = scanPath(targetDir)
    Task.parZip2(sourceScanTask,targeScanTask).flatMap{
      case (Success(sourceScanFiles),Success(targetScanFiles)) =>
        syncScanFiles(sourceScanFiles,targetScanFiles)
      case (sourceScanFilesTry,targetScanFilesTry) =>
        val re = new RuntimeException("FileSynchronizer.sync is faild")
        for(Failure(e)<-sourceScanFilesTry::targetScanFilesTry::Nil){
          re.addSuppressed(e)
        }
        Task.now(Failure(re))
    }
  }
}
