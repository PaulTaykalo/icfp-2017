package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

// looks for most  connected river in minimal spanning tree
object SpanningTree : Strategy<StrategyStateWithGame> {

    var graphUtils: GraphUtils? = null;

    fun init(game: Game) {
        if (graphUtils == null) {
            graphUtils = GraphUtils(game)
        }
    }

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val newGame = applyMoves(moves, state.game)

        init(newGame)
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(), state)
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if (mostConnected.isNotEmpty()) {
            return Pair(newGame.claim(mostConnected.first()), StrategyStateWithGame(newGame))
        }
        // if minimal spanning tree is captured, do whatever is left
        return Pair(newGame.claim(rivers.first()), StrategyStateWithGame(newGame))

    }
}