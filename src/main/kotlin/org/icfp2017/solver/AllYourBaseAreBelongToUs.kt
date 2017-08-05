package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

object AllYourBaseAreBelongToUs : Strategy <StrategyStateWithGame>{
    lateinit var graphUtils: GraphUtils


    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        val rivers = game.unownedRivers.toList()
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