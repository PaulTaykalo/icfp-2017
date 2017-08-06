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
    SpanningTree_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, SpanningTree)
    },
    AllYourBaseAreBelongToUsRandom_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, AllYourBaseAreBelongToUsRandom)
    },
    DumbAndGreedy_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, DumbAndGreedy)
    },
    DumbAndGreedy2_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, DumbAndGreedy2)
    },
    MinMaxRivers_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, AlphaBeta)
    },
    MinMaxScore_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, MinMaxScore)
    },
    SmartAndGreedy_ {
        override fun play(server: OfflineServer) = OfflineSolver.play(server, SmartAndGreedy)
    };

    abstract fun play(server: OfflineServer)

    companion object {
        fun play(server: OfflineServer, name :String = Arguments.strategy) = valueOf(name).play(server)
    }
}