package sbt.ru.bitec.remotebloop

import sbt.internal.inc.Hash

object HashUtiles {
  def fromString(s: String): Option[Hash] = {
    Hash.fromString(s)
  }
}
