package ru.bitec.remotebloop.rbpcommander

object ProcessBuilder {
  def bloopBsp(): ProcessBuilder = {
    new ProcessBuilder("cmd","/Q","/c","bin\\bloopbsp.cmd")
  }
}
