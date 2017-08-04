package org.icfp2017.solver

import org.icfp2017.server.Server

object Solver {
    fun play(server: Server, name: String = "Lambada Punter") {
        server.me(name) {
            server.setup { game ->
                server.ready(game.punter)
                server.onMove { moves -> game.move(moves) }
            }
        }
    }
}