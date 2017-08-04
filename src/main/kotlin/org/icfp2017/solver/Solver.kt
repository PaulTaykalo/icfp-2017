package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.Server
import java.util.*

object FirstFree: Strategy {
    override fun move(game: Game): Move {
        return game.claim(game.map.rivers.unclaimed.firstOrNull())
    }
}

object RandomFree: Strategy {
    val random = Random()
    override fun move(game: Game): Move {
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        return game.claim(rivers[random.nextInt(rivers.size)])
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