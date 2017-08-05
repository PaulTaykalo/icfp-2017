package org.icfp2017.solver

import org.icfp2017.Arguments
import org.icfp2017.Logger
import org.icfp2017.nameWithStrategy
import org.icfp2017.server.OfflineServer

object OfflineSolver {
    fun play(server: OfflineServer, name: String = Arguments.nameWithStrategy, strategy: Strategy<StrategyStateWithGame> = Arguments.strategy) {
        server.me(name) {

            server.setup(
                onSetup = { game ->
                    val state = strategy.prepare(game)
                    server.ready(game.punter, state)
                },
                onMove = { moves, state ->
                    Logger.measure("strategy") {
                        strategy.serverMove(moves, state)
                    }
                },
                onEnd = {},
                onInterruption = {}
            )
        }
    }
}