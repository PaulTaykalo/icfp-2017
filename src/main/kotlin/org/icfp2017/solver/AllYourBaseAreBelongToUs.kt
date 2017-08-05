package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import org.icfp2017.server.ServerMove

object AllYourBaseAreBelongToUs : Strategy <StrategyStateWithGame> {
    lateinit var graphUtils: GraphUtils


    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val game = state.game
        game.apply(moves)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(game.pass(), state)

        val baseRivers = graphUtils.riversCloseToBases(rivers, game.map)
        if (baseRivers.isNotEmpty()) {
            return Pair(game.claim(baseRivers.first()), state)
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if (mostConnected.isNotEmpty()) {
            return Pair(game.claim(mostConnected.first()), state)
        }

        // if minimal spanning tree is captured, do whatever is left
        return Pair(game.claim(rivers.first()), state)
    }
}