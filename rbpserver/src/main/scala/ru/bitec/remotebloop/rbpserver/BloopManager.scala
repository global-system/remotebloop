package ru.bitec.remotebloop.rbpserver
import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.ConcurrentHashMap

class BloopManager(processBuilder:ProcessBuilder,log: String => Unit){
  var process:Process = _
  def start():Unit = {
    process = processBuilder.start()
    val inStream = process.getInputStream
    val thread = new Thread({()=>
      val reader = new InputStreamReader(inStream)
      val bufReader = new BufferedReader(reader)
      try{
        val startMsg = "the bloop process is started"
        println(startMsg)
        log(startMsg)
        var line = bufReader.readLine()
        while(line!=null){
          log(line)
          line = bufReader.readLine()
        }
      }finally{
        bufReader.close()
        reader.close()
        inStream.close()
      }
      val endMsg = "the bloop process is destroyed"
      log(endMsg)
      println(endMsg)
    })
    thread.start()
  }
  def stop(): Unit ={
    val pb = ProcessBuilder.stopBloop()
    val p = pb.start()
    p.waitFor()
    process.destroy()
  }
}

object BloopManager {
   var bloopManager:Option[BloopManager] = None
   var isShutDown:Boolean = false
   val listenersMap: ConcurrentHashMap[BloopManagerListener,BloopManagerListener] = new ConcurrentHashMap()
   def startBloop():Unit = this.synchronized{
     stopBloop()
     val pb = ProcessBuilder.startBloop
     bloopManager = Some( new BloopManager(pb,{msg=>
       listenersMap.forEach{(key,value)=>
         key.onLog(msg)
       }
     }))
     bloopManager.get.start()
   }

   def stopBloop():Unit = this.synchronized{
     bloopManager.foreach{ bm =>
       bm.stop()
       bloopManager = None
     }
   }

  def subscribe(bloopManagerListener: BloopManagerListener):Unit = this.synchronized{
    if (isShutDown){
      bloopManagerListener.onShutDown()
    }else{
      listenersMap.put(bloopManagerListener,bloopManagerListener)
    }

  }
  def unSubscribe(bloopManagerListener: BloopManagerListener):Unit =this.synchronized {
    listenersMap.remove(bloopManagerListener)
  }

  def shutDown():Unit =this.synchronized {
    isShutDown = true
    listenersMap.forEach{(key,value)=>
      key.onShutDown()
    }
    listenersMap.clear()
    stopBloop()
  }
  def isBloopStarted():Boolean = this.synchronized{
    bloopManager.nonEmpty
  }
}
