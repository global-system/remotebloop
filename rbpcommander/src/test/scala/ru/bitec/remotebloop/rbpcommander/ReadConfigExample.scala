package ru.bitec.remotebloop.rbpcommander

import java.nio.file.Paths


object ReadConfigExample {

  def main(args: Array[String]): Unit = {
     import bloop.config.read
     val configPath =  Paths.get(".bloop/rbpcommander.json").toAbsolutePath
     val config = read(configPath)
     println(config)
  }
}
