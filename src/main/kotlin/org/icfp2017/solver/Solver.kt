package org.icfp2017.solver

import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph
import com.sun.javaws.exceptions.InvalidArgumentException
import org.icfp2017.*
import org.icfp2017.Map
import org.icfp2017.graph.findMostAdjacentEdgeInSpanningTree
import org.icfp2017.server.Server
import java.util.*
import org.icfp2017.graph.*


object FirstFree: Strategy {
    override fun move(game: Game): Move {
        return game.claim(game.map.rivers.unclaimed.firstOrNull())
    }
}

object RandomFree: Strategy {
    val random = Random()
    override fun move(game: Game): Move {
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        return game.claim(rivers[random.nextInt(rivers.size)])
    }
}
// looks for most  connected river in minimal spanning tree
object SpanningTree : Strategy{
    fun mostConnectedRivers(rivers: List<River>, map: Map) : List<River>{
        val edge = findMostAdjacentEdgeInSpanningTree(map)
        return rivers.filter { (it.source == edge.source && it.target == edge.target)|| ((it.source == edge.target && it.target == edge.source)) }
    }

    override fun move(game: Game): Move {
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()
        val mostConnected = mostConnectedRivers(rivers, game.map)
        if(mostConnected.isNotEmpty()){
            return game.claim(mostConnected.first())
        }
        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())

    }
}

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUs : Strategy{

    fun mostConnectedRivers(rivers: List<River>, map: Map) : List<River>{
        val edge = findMostAdjacentEdgeInSpanningTree(map)
        return rivers.filter { (it.source == edge.source && it.target == edge.source)|| ((it.source == edge.target && it.target == edge.source)) }
    }

    fun riversCloseToBases(rivers:List<River>, map:Map, graph: UWGraph):List<River> {
        val baseRivers = rivers.filter {  map.mines.contains(it.target) || map.mines.contains(it.source)}
        val priorityBaseRivers = baseRivers.sortedWith(compareBy({graph.adjacentEdges(it.target).size},{graph.adjacentEdges(it.source).size}))
        return  priorityBaseRivers
    }

    override fun move(game: Game): Move {

        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()
        val graph = toGraph(game.map)
        val baseRivers = riversCloseToBases(rivers, game.map, graph)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = mostConnectedRivers(rivers, game.map)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }

        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())
    }
}


interface Strategy {
    fun move(game: Game): Move

    companion object {
        fun forName(name: String) = when(name) {
            "SpanningTree" -> SpanningTree
            "First" -> FirstFree
            "Random" -> RandomFree
            "AllYourBaseAreBelongToUs" -> AllYourBaseAreBelongToUs
            else -> throw InvalidArgumentException(arrayOf("Unknown strategy name"))
        }
    }
}

object Solver {
    fun play(server: Server, name: String = Arguments.name, strategy: Strategy = Arguments.strategy) {
        server.me(name) {
            server.setup { game ->
                server.ready(
                    punterID = game.punter,
                    onMove = { moves ->
                        game.map.apply(moves)
                        strategy.move(game)
                    },
                    onInterruption = {

                    },
                    onEnd = {

                    }
                )
            }
        }
    }
}