package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import org.icfp2017.server.ServerMove
import java.util.*

object EagerBaseCatcher : Strategy<StrategyStateWithGame>{
    val random = Random()
    lateinit var graphUtils: GraphUtils

    override fun prepare(game: Game): StrategyStateWithGame {
        graphUtils = GraphUtils(game)
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): ServerMove {
        val game = state.game
        game.apply(moves)
        graphUtils.updateState(game)

        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return ServerMove(game.pass(), state)

        var path :Iterable<River> = listOf();

        for (mine1 in game.mines){
            for(mine2 in game.mines){
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
                        return ServerMove(game.claim(intersection.first()), state)
                    }
                    //Logger.log("intersection is empty")
                }
            }
        }


//        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
//        if(baseRivers.isNotEmpty()){
//            return ServerMove(game.claim(baseRivers.first()), state)
//        }
//        // if all base rivers are captures, do most connected things
//        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
//        if(mostConnected.isNotEmpty()) {
//            return game.claim(mostConnected.first())
//        }

        // if minimal spanning tree is captured, do whatever is left
        return ServerMove(game.claim(rivers[random.nextInt(rivers.size)]), state)
    }
}