package ru.bitec.remotebloop.rbpserver;

object ProcessBuilder {
   def stopBloop(): ProcessBuilder = {
     new ProcessBuilder("cmd","/c","bin\\stopbloop.cmd")
   }
  def startBloop: ProcessBuilder = {
    new ProcessBuilder("cmd","/c","bin\\startbloop.cmd")
  }
}
