package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import java.util.*

object EagerBaseCatcher : Strategy<StrategyStateWithGame>{
    val random = Random()
    lateinit var graphUtils: GraphUtils

    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {

        val newGame = applyMoves(moves, state.game)
        graphUtils.updateState(newGame.sitesForSite, newGame.unownedRivers, newGame.ownedRivers, newGame.myRivers)

        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(), state)

        var path :Iterable<River> = listOf();

        for (mine1 in newGame.mines){
            for(mine2 in newGame.mines){
                if(mine1!=mine2){
                    path = graphUtils.findPath(mine1, mine2)
                    if(path.count() == 0){
                    //    Logger.log("path bewtween $mine1 and $mine2 not found")
                        continue
                    }
                   // Logger.log("path between $mine1 and $mine2 is " +path)
                    //Logger.log("free rivers are " + graphUtils.freeRivers)
                    val intersection = graphUtils.freeRivers.intersect(path)

                    if(intersection.isNotEmpty())
                    {
                      //  Logger.log("intersection is " + intersection.first())
                        return Pair(newGame.claim(intersection.first()), state)
                    }
                    //Logger.log("intersection is empty")
                }
            }
        }


//        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
//        if(baseRivers.isNotEmpty()){
//            return Pair(game.claim(baseRivers.first()), state)
//        }
//        // if all base rivers are captures, do most connected things
//        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
//        if(mostConnected.isNotEmpty()) {
//            return game.claim(mostConnected.first())
//        }

        // if minimal spanning tree is captured, do whatever is left
        return Pair(newGame.claim(rivers[random.nextInt(rivers.size)]), StrategyStateWithGame(newGame))
    }
}