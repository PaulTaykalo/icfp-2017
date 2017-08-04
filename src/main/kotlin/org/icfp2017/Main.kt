@file:JvmName("Main")

package org.icfp2017

import org.icfp2017.server.OnlineServer
import org.icfp2017.solver.*

object Arguments {
    var name: String = "Lambada Punter"
    var server: String = "punter.inf.ed.ac.uk"
    var port: Int = 9024
    var strategy: Strategy = RandomFree
}

fun main(args: Array<String>) {
    args.forEach {
        val (name, value) = it.split("=")
        when (name) {
            "--name" -> Arguments.name = value
            "--server" -> Arguments.server = value
            "--port" -> Arguments.port = value.toInt()
            "--strategy" -> Arguments.strategy = Strategy.forName(value)
        }
    }

    println(
            """
Arguments
      name: ${Arguments.name}
      server: ${Arguments.server}
      port: ${Arguments.port}
      strategy: ${Arguments.strategy}
"""
    )

    val server = OnlineServer()
    Solver.play(server)
}