package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import java.util.*

object EagerBaseCatcher : Strategy{
    val random = Random()
    lateinit var graphUtils: GraphUtils

    override fun prepare(game: Game){
        graphUtils = GraphUtils(game)
    }

    override fun move(game: Game): Move {
        graphUtils.updateState(game)

        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return game.pass()

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
                        return game.claim(intersection.first())
                    }
                    //Logger.log("intersection is empty")
                }
            }
        }


//        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
//        if(baseRivers.isNotEmpty()){
//            return game.claim(baseRivers.first())
//        }
//        // if all base rivers are captures, do most connected things
//        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
//        if(mostConnected.isNotEmpty()) {
//            return game.claim(mostConnected.first())
//        }

        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers[random.nextInt(rivers.size)])
    }
}