package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
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
        val newGame = applyMoves(moves, state.game)
        graphUtils.updateState(newGame)
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(), state)

        val baseRivers =  graphUtils.riversCloseToBases(rivers, newGame)
        if(baseRivers.isNotEmpty()){
            val claim = baseRivers.first()
            //Logger.log("base river claim $claim")
            return Pair(newGame.claim(claim), state)
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            val claim = mostConnected.first()
            //Logger.log("most connected claim $claim")
            return Pair(newGame.claim(claim), state)
        }


        // if minimal spanning tree is captured, do whatever is left
        return  Pair(newGame.claim(rivers[random.nextInt(rivers.size)]), state)
    }
}