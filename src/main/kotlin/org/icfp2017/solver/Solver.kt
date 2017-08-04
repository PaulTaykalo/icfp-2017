package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.Map
import org.icfp2017.graph.findMostAdjacentEdgeInSpanningTree
import org.icfp2017.server.Server
import java.util.*

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
        return rivers.filter { (it.source == edge.v && it.target == edge.w)|| ((it.source == edge.w && it.target == edge.v)) }
    }

    override fun move(game: Game): Move {
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()
        val mostConnected = mostConnectedRivers(rivers, game.map)
        if(mostConnected.isEmpty()) return  game.pass()
        return game.claim(mostConnected.first())
    }
}

interface Strategy {
    fun move(game: Game): Move
}

object Solver {
    fun play(server: Server, name: String = "Lambada Punter", strategy: Strategy = FirstFree) {
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