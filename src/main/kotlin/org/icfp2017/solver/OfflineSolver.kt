package org.icfp2017.solver

import org.icfp2017.Arguments
import org.icfp2017.Logger
import org.icfp2017.nameWithStrategy
import org.icfp2017.server.OfflineServer

object OfflineSolver {
    inline fun <reified T>play(server: OfflineServer, strategy: Strategy<T>, name: String = Arguments.nameWithStrategy) {
        server.me(name) {

            server.setup(
                onSetup = { game ->
                    val state = strategy.prepare(game)
                    val futures = strategy.futures()
                    server.ready(game.punter, futures, state)
                },
                onMove = { moves, state: T ->
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