package org.icfp2017.solver

import org.icfp2017.Arguments
import org.icfp2017.nameWithStrategy
import org.icfp2017.server.OfflineServer
import org.icfp2017.server.ServerMove

object OfflineSolver {
    inline fun <reified T>play(server: OfflineServer, name: String = Arguments.nameWithStrategy, strategy: Strategy<T> = Arguments.strategy as Strategy<T>) {
        server.me(name) {

            server.setup(
                onSetup = { game ->
                    val state = strategy.prepare(game)
                    server.ready(game.punter, state)
                },
                onMove = { moves, state: T ->
                    strategy.serverMove(moves, state)
                },
                onEnd = {},
                onInterruption = {}
            )
        }
    }
}