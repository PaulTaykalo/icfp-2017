package org.icfp2017.solver

import com.google.gson.annotations.SerializedName
import com.sun.javaws.exceptions.InvalidArgumentException
import com.sun.org.apache.xpath.internal.Arg
import org.icfp2017.Arguments
import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.server.OfflineServer

data class StrategyStateWithGame(
    @SerializedName("game") val game: Game
)

interface Strategy<State> {

    fun serverMove(moves: Array<Move>, state: State): Pair<Move, State>
    fun prepare(game: Game): State

    companion object {
        fun play(server: OfflineServer, name: String = Arguments.strategy) = when (name) {
            "SpanningTree" -> OfflineSolver.play(server, strategy = SpanningTree)
            "First" -> OfflineSolver.play(server, strategy = FirstFree)
            "Random" -> OfflineSolver.play(server, strategy = RandomFree)
            "AllYourBaseAreBelongToUsRandom" -> OfflineSolver.play(server, strategy = AllYourBaseAreBelongToUsRandom)
            "DumbAndGreedy" -> OfflineSolver.play(server, strategy = DumbAndGreedy)
            "EagerBaseCatcher" -> OfflineSolver.play(server, strategy = EagerBaseCatcher)
            else -> throw InvalidArgumentException(arrayOf("Unknown strategy name"))
        }
    }
}