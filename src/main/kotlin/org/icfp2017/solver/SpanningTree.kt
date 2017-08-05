package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

// looks for most  connected river in minimal spanning tree
object SpanningTree : Strategy{

    var graphUtils: GraphUtils? = null;

    fun init(game: Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
        graphUtils!!.updateState(game)
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = graphUtils!!.freeRivers
        if (rivers.isEmpty()) return game.pass()
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()){
            return game.claim(mostConnected.first())
        }
        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())

    }
}