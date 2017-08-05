package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

// looks for most  connected river in minimal spanning tree
object SpanningTree : Strategy<StrategyStateWithGame>{

    var graphUtils: GraphUtils? = null;

    fun init(game: Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
    }
    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        init(game)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return game.pass()
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()){
            return game.claim(mostConnected.first())
        }
        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())

    }
}