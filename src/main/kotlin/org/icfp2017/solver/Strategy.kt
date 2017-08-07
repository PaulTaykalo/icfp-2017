package org.icfp2017.solver

import com.google.gson.annotations.SerializedName
import com.sun.org.apache.xpath.internal.Arg
import org.icfp2017.Arguments
import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.server.FutureRequest
import org.icfp2017.server.OfflineServer
import org.icfp2017.solver.alphaBeta.AlphaBeta
import org.icfp2017.solver.alphaBeta.MinMaxScore
import org.icfp2017.solver.alphaBeta.MinMaxScoreSpanning

data class StrategyStateWithGame(
        @SerializedName("game") val game: Game
)

interface Strategy<State> {

    fun serverMove(moves: Array<Move>, state: State): Pair<Move, State>
    fun prepare(game: Game): State
    fun futures(): Array<FutureRequest>? {
        return null
    }
}

enum class Strategies {
    SpanningTree_,
    AllYourBaseAreBelongToUsRandom_,
    DumbAndGreedy_,
    DumbAndGreedy2_,
    MinMaxRivers_ ,
    MinMaxScore_ ,
    MinMaxScoreSpanning_,
    GreedyLover_,
    GreedySharpLover_,
    GreedyLover2_;

    companion object {
        fun play(server: OfflineServer, name: String) = when(valueOf(name)) {
            SpanningTree_ -> OfflineSolver.play(server, SpanningTree)
            AllYourBaseAreBelongToUsRandom_ -> OfflineSolver.play(server, AllYourBaseAreBelongToUsRandom)
            DumbAndGreedy_ -> OfflineSolver.play(server, DumbAndGreedy)
            DumbAndGreedy2_ -> OfflineSolver.play(server, DumbAndGreedy2)
            MinMaxRivers_ -> OfflineSolver.play(server, AlphaBeta)
            MinMaxScore_ -> OfflineSolver.play(server, MinMaxScore)
            MinMaxScoreSpanning_ -> OfflineSolver.play(server, MinMaxScoreSpanning)
            GreedyLover_ -> OfflineSolver.play(server, GreedyLover)
            GreedySharpLover_ -> OfflineSolver.play(server, GreedySharpLover)
            GreedyLover2_ -> OfflineSolver.play(server, GreedyLover2)
        }
    }
}