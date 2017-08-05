package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

object AllYourBaseAreBelongToUs : Strategy{
    var graphUtils: GraphUtils? = null;

    fun init(game: Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
        graphUtils!!.updateState(game)
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }

        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())
    }
}