package org.icfp2017.solver

import com.google.gson.annotations.SerializedName
import org.icfp2017.Arguments
import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.server.FutureRequest
import org.icfp2017.server.OfflineServer
import org.icfp2017.solver.alphaBeta.AlphaBeta

data class StrategyStateWithGame(
    @SerializedName("game") val game: Game
)

interface Strategy<State> {

    fun serverMove(moves: Array<Move>, state: State): Pair<Move, State>
    fun prepare(game: Game): State
    fun futures(): Array<FutureRequest>? {
        return null
    }

    companion object {

        val strategyFactory: Map<String, (OfflineServer) -> Unit> = mapOf(
            "SpanningTree" to { server -> OfflineSolver.play(server, strategy = SpanningTree) },
//            "First" to { server -> OfflineSolver.play(server, strategy = FirstFree) },
//            "Random" to { server -> OfflineSolver.play(server, strategy = RandomFree) },
            "AllYourBaseAreBelongToUsRandom" to { server -> OfflineSolver.play(server, strategy = AllYourBaseAreBelongToUsRandom) },
            "AllYourBaseAreBelongToUsExpansion" to { server -> OfflineSolver.play(server, strategy = AllYourBaseAreBelongToUsExpansion) },
            "DumbAndGreedy" to { server -> OfflineSolver.play(server, strategy = DumbAndGreedy) },
            "SmartAndGreedy" to { server -> OfflineSolver.play(server, strategy = SmartAndGreedy) },
                "MinMax" to { server -> OfflineSolver.play(server, strategy = AlphaBeta) },
            "EagerBaseCatcher" to { server -> OfflineSolver.play(server, strategy = EagerBaseCatcher) }
        )

        fun play(server: OfflineServer, name: String = Arguments.strategy) = strategyFactory[name]!!(server)
    }
}