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

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): ServerMove {
        val game = state.game
        game.apply(moves)

        init(game)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return ServerMove(game.pass(), state)
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if (mostConnected.isNotEmpty()) {
            return ServerMove(game.claim(mostConnected.first()), state)
        }
        // if minimal spanning tree is captured, do whatever is left
        return ServerMove(game.claim(rivers.first()), state)

    }
}