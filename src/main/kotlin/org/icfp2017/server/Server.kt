package org.icfp2017.server

import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.PunterID
import org.icfp2017.PunterName
import org.icfp2017.base.StopCommand


interface Server {

  // Send me once we ready
  // me == Usernam
  fun me(me: PunterName, callback: (PunterName) -> Unit)

  // Send setup and wait for the callback
  fun setup(callback: (Game) -> Unit)

  // Send after setup completed
  fun ready(punterID: PunterID)

  fun onMove(observer: (Array<Move>) -> Move)

  // Subscribe if you want to stop any calcution
  fun onInterruption(callback: (String) -> Unit)

  ///
  fun onEnd(callback: (StopCommand) -> Unit)

}

