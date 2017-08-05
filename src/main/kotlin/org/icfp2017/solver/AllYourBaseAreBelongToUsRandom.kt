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

    override fun serverMove (moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val game = state.game
        graphUtils.updateState(game)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(game.pass(), state)

        val baseRivers =  graphUtils.riversCloseToBases(rivers, game)
        if(baseRivers.isNotEmpty()){
            val claim = baseRivers.first()
            //Logger.log("base river claim $claim")
            return Pair(game.claim(claim), state)
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            val claim = mostConnected.first()
            //Logger.log("most connected claim $claim")
            return Pair(game.claim(claim), state)
        }


        // if minimal spanning tree is captured, do whatever is left
        return  Pair(game.claim(rivers[random.nextInt(rivers.size)]), state)
    }
}