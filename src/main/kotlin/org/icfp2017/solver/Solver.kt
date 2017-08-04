package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.Server

object FirstFree: Strategy {
    override fun move(game: Game): Move {
        val river = game.map.rivers.unclaimed.firstOrNull()

        return if (river != null) Claim(game.punter, river.source, river.target)
        else Pass(game.punter)
    }
}

interface Strategy {
    fun move(game: Game): Move
}

object Solver {
    fun play(server: Server, name: String = "Lambada Punter", strategy: Strategy = FirstFree) {
        server.me(name) {
            server.setup { game ->
                server.ready(game.punter)
                server.onMove { moves ->
                    game.map.apply(moves)
                    strategy.move(game)
                }
            }
        }
    }
}