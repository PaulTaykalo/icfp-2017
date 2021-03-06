package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import java.util.*

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsRandom : Strategy<StrategyStateWithGame>{

    val random = Random()


    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove (moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val graphUtils = GraphUtils(state.game)
        val newGame = applyMoves(moves, state.game)
        graphUtils.updateState(
                newGame.sitesForSite,
                newGame.unownedRivers,
                newGame.ownedRivers,
                newGame.myRivers
        )
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(),  StrategyStateWithGame(newGame))

        val baseRivers =  graphUtils.riversCloseToBases(rivers, newGame.mines, newGame.riversForSite)
        if(baseRivers.isNotEmpty()){
            val claim = baseRivers.first()
            //Logger.log("base river claim $claim")
            return Pair(newGame.claim(claim), StrategyStateWithGame(newGame))
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            val claim = mostConnected.first()
            //Logger.log("most connected claim $claim")
            return Pair(newGame.claim(claim), StrategyStateWithGame(newGame))
        }


        // if minimal spanning tree is captured, do whatever is left
        return  Pair(newGame.claim(rivers.first()), StrategyStateWithGame(newGame))
    }
}