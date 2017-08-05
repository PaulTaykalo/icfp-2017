package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import org.icfp2017.server.ServerMove

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
        val game = state.game
        game.apply(moves)

        init(game)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(game.pass(), state)
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if (mostConnected.isNotEmpty()) {
            return Pair(game.claim(mostConnected.first()), state)
        }
        // if minimal spanning tree is captured, do whatever is left
        return Pair(game.claim(rivers.first()), state)

    }
}