package org.icfp2017.solver

import org.icfp2017.Arguments
import org.icfp2017.nameWithStrategy
import org.icfp2017.server.OfflineServer

object OfflineSolver {
    fun <T>play(server: OfflineServer<T>, name: String = Arguments.nameWithStrategy, strategy: Strategy<T> = Arguments.strategy as Strategy<T>) {
        server.me(name) {

            server.setup(
                onSetup = { game ->
                    val state = strategy.prepare(game)
                    server.ready(game.punter, state)
                },
                onMove = { moves, state ->
                    strategy.serverMove(moves, state)
                },
                onEnd = {},
                onInterruption = {}
            )
        }
    }
}