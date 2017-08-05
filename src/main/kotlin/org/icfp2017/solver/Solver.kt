package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.Server


object Solver {
    fun play(server: Server, name: String = Arguments.name, strategy: Strategy = Arguments.strategy) {
        server.me(name) {
            server.setup { game ->
                strategy.prepare(game)
                server.ready(
                    punterID = game.punter,
                    onMove = { moves ->
                        game.apply(moves)
                        strategy.move(game)
                    },
                    onInterruption = {

                    },
                    onEnd = {

                    }
                )
            }
        }
    }
}