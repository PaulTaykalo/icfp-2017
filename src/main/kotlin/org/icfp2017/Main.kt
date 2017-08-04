@file:JvmName("Main")
package org.icfp2017

import org.icfp2017.server.OnlineServer
import org.icfp2017.solver.Solver

fun main(args : Array<String>) {
  println("Hello, world!")

  println("Args passed :")
  args.forEach { println(it) }

  val server = OnlineServer()
  Solver.play(server)
}