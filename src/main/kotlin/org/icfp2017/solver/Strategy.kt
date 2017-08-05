package org.icfp2017.solver

import com.google.gson.annotations.SerializedName
import com.sun.javaws.exceptions.InvalidArgumentException
import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.server.ServerMove


interface StrategyState {}

class DummyState: StrategyState

data class StrategyStateWithGame(
    @SerializedName("game") val game: Game
): StrategyState

interface Strategy <State: StrategyState> {
    fun move(moves: Array<Move>, state: State): Move

    fun serverMove(moves: Array<Move>, state: State): ServerMove {
        return ServerMove(move(moves, state), state as? StrategyStateWithGame)
    }

    fun prepare(game: Game): State

    companion object {
        fun forName(name: String) = when(name) {
            "SpanningTree" -> SpanningTree
            "First" -> FirstFree
            "Random" -> RandomFree
            "AllYourBaseAreBelongToUs" -> AllYourBaseAreBelongToUs
            "AllYourBaseAreBelongToUsRandom" -> AllYourBaseAreBelongToUsRandom
            "DumbAndGreedy" -> DumbAndGreedy
            "EagerBaseCatcher" -> EagerBaseCatcher
            else -> throw InvalidArgumentException(arrayOf("Unknown strategy name"))
        }
    }
}