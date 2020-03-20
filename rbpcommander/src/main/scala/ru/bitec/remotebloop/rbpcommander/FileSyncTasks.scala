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
case class FilePath(key: String,path: Path,level: Int,hash:String) extends ScanPath
case class ScanFiles(root:Path, dirs: Map[String,DirPath],files: Map[String,FilePath])
case class SyncAction(
                       needDeleteFiles: List[FilePath],
                       needDeleteDirs: List[DirPath],
                       needReplaceFiles: List[FilePath],
                       needCreateDir: List[DirPath]
                     )

class ScanFileVisitor(val rootPath: Path) extends SimpleFileVisitor[Path]{
  var level:Int = 0
  private val dirListBuffer = ArrayBuffer.empty[DirPath]
  private val fileListBuffer = ArrayBuffer.empty[FilePath]
  override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
    val key = rootPath.relativize(dir).toString.replace('\\','/').stripSuffix("/")
    dirListBuffer.append(DirPath(key,dir,level))
    level = level +1;
    super.preVisitDirectory(dir, attrs)
  }

  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    val key = rootPath.relativize(file).toString.replace('\\','/')
    fileListBuffer.append(FilePath(key,file,level,""))
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
    Files.walkFileTree(sourceDir, visitor)
    val result = visitor.build()
    //val end = System.nanoTime()-start
    //println(s"scan ${sourcePath.getFileSystem},$sourcePath \r\n in $end nanos")
    result
  }
  def syncScanFiles(source: ScanFiles,target: ScanFiles): Task[Try[Int]] = TryTask{
    val needDeleteFiles = ArrayBuffer.empty[FilePath]
    target.files.foreach{case (key,file) =>
      if (!source.files.isDefinedAt(key)) {
        needDeleteFiles.append(file)
      }
    }
    val needDeleteDirs = ArrayBuffer.empty[DirPath]
    target.dirs.foreach{case (key,dir) =>
      if (!source.dirs.isDefinedAt(key)) {
        needDeleteDirs.append(dir)
      }
    }
    val needReplaceFiles = ArrayBuffer.empty[FilePath]
    source.files.foreach{case (fromKey,fromFile) =>
      target.files.get(fromKey) match {
        case Some(toFile) if toFile.hash == fromFile.hash =>
        case _ =>
          needReplaceFiles.append(fromFile)
      }
    }
    val needCreateDir = ArrayBuffer.empty[DirPath]
    source.dirs.foreach{case (fromKey,fromDir) =>
      target.dirs.get(fromKey) match {
        case Some(_)  =>
        case _ =>
          needCreateDir.append(fromDir)
      }
    }
    SyncAction(
      needDeleteFiles.toList,
      needDeleteDirs.toList,
      needReplaceFiles.toList,
      needCreateDir.toList
    )
  }.tryFlatMap{syncAction =>
    val sep = target.root.getFileSystem.getSeparator
    val deleteFilesTask = TryTask{
      var count = 0
      syncAction.needDeleteFiles.foreach{file =>
        Files.delete(file.path)
        count=count+1
      }
      count
    }
    val replaceFilesTaks = TryTask{
      var count = 0
      syncAction.needReplaceFiles.foreach{file =>
        val pathTo = target.root.resolve(file.keyToPathString(sep))
        val pathFrom = file.path
        Files.copy(pathFrom,pathTo,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES)
        count=count+1
      }
      count
    }
    val deleteDirTask = TryTask{
      var count = 0
      syncAction.needDeleteDirs.sortBy(-_.level).foreach{dir =>
        val dirPath = target.root.resolve(dir.keyToPathString(sep))
        Files.delete(dirPath)
        count=count+1
      }
      count
    }
    val createDirTask = TryTask{
      var count = 0
      syncAction.needCreateDir.sortBy(_.level).foreach{dir =>
        val dirPath = target.root.resolve(dir.keyToPathString(sep))
        Files.createDirectory(dirPath)
        count=count+1
      }
      count
    }
    val drFilesTask = Task.gather(deleteFilesTask::replaceFilesTaks::Nil).groupByTry().tryMap{ list =>
      list.sum
    }
    Task.sequence(createDirTask::drFilesTask::deleteDirTask::Nil).groupByTry().tryMap{list =>
      list.sum
    }
  }
  def sync(sourceDir: Path, targetDir: Path): Task[Try[Int]] = {
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
