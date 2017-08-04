package org.icfp2017.server

import org.icfp2017.base.Move


interface Server {

  fun send(command: Move)

  fun ready()

}

class ServerTCP : Server {
  override fun ready() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun send(command: Move) {

  }
}

class ServerInputOutput : Server {
  override fun ready() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun send(command: Move) {

  }

}

