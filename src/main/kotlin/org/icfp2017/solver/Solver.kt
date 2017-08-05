package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.Server
import org.icfp2017.server.ServerMove


object Solver {
    fun play(server: Server, name: String = Arguments.nameWithStrategy, strategy: Strategy<StrategyStateWithGame> = Arguments.strategy) {
        server.me(name) {
            server.setup { game ->
                val initialState: StrategyStateWithGame? = null
                strategy.prepare(game)
                server.ready(
                    punterID = game.punter,
                    state = initialState,
                    onMove = { moves, state ->
                        Logger.measure("strategyDuration") {
                            game.apply(moves)
                            strategy.serverMove(moves, state ?: StrategyStateWithGame(game))
                        }
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