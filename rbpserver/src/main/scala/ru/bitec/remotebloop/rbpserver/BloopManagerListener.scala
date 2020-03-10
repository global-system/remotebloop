package ru.bitec.remotebloop.rbpserver;

trait BloopManagerListener {
  def onLog(line :String): Unit
  def onShutDown(): Unit
}
