package sbt.ru.bitec.remotebloop

import java.io.File

import sbt.internal.inc.Hash


object HashUtiles {
  def fromString(s: String): Option[Hash] = {
    Hash.fromString(s)
  }
  def ofFile(file: File): Hash = {
    Hash.ofFile(file)
  }
}
