package ru.bitec.remotebloop.rbpcommander

import java.nio.file.Paths


object ReadConfigExample {
  import bloop.config.read
  def main(args: Array[String]): Unit = {
     val configPath =  Paths.get(".bloop/rbpcommander.json").toAbsolutePath
     val config = read(configPath)
     println(config)
  }
}
