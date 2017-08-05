package org.icfp2017.solver

import com.sun.javaws.exceptions.InvalidArgumentException
import org.icfp2017.*
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

    var graphUtils: GraphUtils? = null;

    fun init(game:Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()){
            return game.claim(mostConnected.first())
        }
        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())

    }
}
object AllYourBaseAreBelongToUs : Strategy{
    var graphUtils: GraphUtils? = null;

    fun init(game:Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }

        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers.first())
    }
}


// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsRandom : Strategy{
    val random = Random()
    var graphUtils: GraphUtils? = null;

    fun init(game:Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }


        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers[random.nextInt(rivers.size)])
    }
}

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsConnectBases : Strategy{
    val random = Random()
    var graphUtils: GraphUtils? = null;

    fun init(game:Game){
        if(graphUtils == null){
            graphUtils = GraphUtils(game)
        }
    }

    override fun move(game: Game): Move {
        init(game)
        val rivers = game.map.rivers.unclaimed
        if (rivers.isEmpty()) return game.pass()

        val baseRivers =  graphUtils!!.riversCloseToBases(rivers, game.map)
        if(baseRivers.isNotEmpty()){
            return game.claim(baseRivers.first())
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils!!.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            return game.claim(mostConnected.first())
        }

        //game.punter
        // if minimal spanning tree is captured, do whatever is left
        return  game.claim(rivers[random.nextInt(rivers.size)])
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
            "AllYourBaseAreBelongToUsRandom" -> AllYourBaseAreBelongToUsRandom
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