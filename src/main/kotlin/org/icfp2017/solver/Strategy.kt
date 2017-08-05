package org.icfp2017.solver

import com.google.gson.annotations.SerializedName
import com.sun.javaws.exceptions.InvalidArgumentException
import org.icfp2017.Game
import org.icfp2017.Move

data class StrategyStateWithGame(
    @SerializedName("game") val game: Game
)

interface Strategy<State> {

    fun serverMove(moves: Array<Move>, state: State): Pair<Move, State>
    fun prepare(game: Game): State

    companion object {
        fun forName(name: String) = when (name) {
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