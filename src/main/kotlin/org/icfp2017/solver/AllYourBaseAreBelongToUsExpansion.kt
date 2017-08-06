package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import java.util.*

data class AllYourBaseAreBelongToUsRandomExpansionState(
        val sitesForSite: SitesForSite,
        val unownedRivers: Set<River>,
        val ownedRivers: Set<River>,
        val myRivers: Set<River>
)

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsRandomExpansion : Strategy<AllYourBaseAreBelongToUsRandomExpansionState>{

    val random = Random()

    override fun prepare(game: Game): AllYourBaseAreBelongToUsRandomExpansionState {
        // TODO: return state
    }

    override fun serverMove (moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val graphUtils = GraphUtils(state.game)
        val newGame = applyMoves(moves, state.game)
        graphUtils.updateState(newGame)
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(),  StrategyStateWithGame(newGame))

        val baseRivers =  graphUtils.riversCloseToBases(rivers, newGame)
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