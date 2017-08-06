package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

object AllYourBaseAreBelongToUs : Strategy <StrategyStateWithGame> {
    lateinit var graphUtils: GraphUtils


    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {

        val newGame = applyMoves(moves, state.game)
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(), state)

        val baseRivers = graphUtils.riversCloseToBases(rivers, newGame)
        if (baseRivers.isNotEmpty()) {
            return Pair(newGame.claim(baseRivers.first()), state)
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if (mostConnected.isNotEmpty()) {
            return Pair(newGame.claim(mostConnected.first()), state)
        }

        // if minimal spanning tree is captured, do whatever is left
        return Pair(newGame.claim(rivers.first()), state)
    }
}