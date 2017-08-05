package org.icfp2017.solver

import org.icfp2017.Arguments
import org.icfp2017.nameWithStrategy
import org.icfp2017.server.OfflineServer
import org.icfp2017.server.Server
import org.icfp2017.server.ServerMove
import org.icfp2017.server.State

object OfflineSolver {
    fun play(server: OfflineServer, name: String = Arguments.nameWithStrategy, strategy: Strategy = Arguments.strategy) {
//        server.me(name) {
//            server.setup(onSetup = { game ->
//                server.ready(game.punter)
//            },
//                    onEnd = {},
//                    onInterruption = {},
//                    onMove = {}
//                    )
//            server.setup(onSetup = { game ->
////                val initialState: State? = null
////                strategy.prepare(game)
////                server.ready(
////                    punterID = game.punter,
////                    state = initialState,
////                    onMove = { moves, state ->
////                        game.apply(moves)
////                        val move = strategy.move(game)
////                        val updatedState: State? = null
////                        ServerMove(move, updatedState)
////                    },
////                    onInterruption = {
////
////                    },
////                    onEnd = {
////
////                    }
////                )
//            }
//        }
    }
}