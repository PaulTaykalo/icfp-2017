package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import org.icfp2017.server.ServerMove
import java.util.*

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsRandom : Strategy<StrategyStateWithGame>{

    val random = Random()
    lateinit var graphUtils: GraphUtils

    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        graphUtils.updateState(game)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return game.pass()

        val baseRivers =  graphUtils.riversCloseToBases(rivers, game.map)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }


        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers[random.nextInt(rivers.size)])
    }
}