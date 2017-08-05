package org.icfp2017.solver

import com.sun.javaws.exceptions.InvalidArgumentException
import org.icfp2017.Game
import org.icfp2017.Move


interface Strategy {
    fun move(game: Game): Move

    companion object {
        fun forName(name: String) = when(name) {
            "SpanningTree" -> SpanningTree
            "First" -> FirstFree
            "Random" -> RandomFree
            "AllYourBaseAreBelongToUs" -> AllYourBaseAreBelongToUs
            "AllYourBaseAreBelongToUsRandom" -> AllYourBaseAreBelongToUsRandom
            "DumbAndGreedy" -> DumbAndGreedy
            else -> throw InvalidArgumentException(arrayOf("Unknown strategy name"))
        }
    }
}