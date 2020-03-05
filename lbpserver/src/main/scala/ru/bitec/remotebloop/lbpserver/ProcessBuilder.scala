package ru.bitec.remotebloop.lbpserver

object ProcessBuilder {
   def stopBloop(): ProcessBuilder = {
     new ProcessBuilder("cmd","/c","bin\\stopbloop.cmd")
   }
  def startBloop: ProcessBuilder = {
    new ProcessBuilder("cmd","/c","bin\\startbloop.cmd")
  }
}
