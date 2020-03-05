package ru.bitec.remotebloop.lbpserver

trait BloopManagerListener {
  def onLog(line :String): Unit
  def onShutDown(): Unit
}
