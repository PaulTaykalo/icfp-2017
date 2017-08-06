package org.icfp2017.alphaBeta

import com.google.gson.Gson
import org.amshove.kluent.`should equal`
import org.icfp2017.Game
import org.icfp2017.MapModel
import org.icfp2017.solver.alphaBeta.getBestMove
import org.junit.Test

class AlphaBetaTest{
    @Test
    fun tester() {
        val mapModelString ="""{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}""";
        val mapModel = Gson().fromJson(mapModelString, MapModel::class.java)
        //val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        //val game = Gson().fromJson(modelString, Game::class.java)


        val fullGame = Game(0, 2,mapModel)


        val node = getBestMove(fullGame,2)

        node.toString()
//        |-/\-|
//        |_\/_|
    }
}