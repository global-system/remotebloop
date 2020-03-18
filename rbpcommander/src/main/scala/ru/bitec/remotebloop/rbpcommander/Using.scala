package ru.bitec.remotebloop.rbpcommander
class  Using[T <: AutoCloseable](openResource: () => T){
  def foreach(body: T => Unit): Unit = {
    val resource = openResource()
    try{
      body(resource)
    }finally{
      resource.close()
    }
  }
}

object Using {
  def apply[T <: AutoCloseable](body: => T):Using[T] = {
     new Using[T](body _ )
  }
}
