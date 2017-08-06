package org.icfp2017

import com.google.gson.Gson
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not contain`
import org.junit.Test

class GameTest {

    @Test
    fun tester() {
        val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        val game = Gson().fromJson(modelString, Game::class.java)
        game.punter `should equal` 0
        game.punters `should equal` 2
//        |-/\-|
//        |_\/_|
    }

    @Test
    fun testScores() {
        val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        var game = Gson().fromJson(modelString, Game::class.java)
        game = Game(punter = game.punter, punters = game.punters, mapModel = game.mapModel, settings = null)
        game.siteScores[0]!![1]!! `should equal` 1L
        game.siteScores[6]!![1]!! `should equal` 4L
        game.siteScores[4]!![1]!! `should equal` 4L
        game.siteScores[5]!![1]!! `should equal` 4L
    }

    @Test
    fun testApplySplurges() {
        val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        var game = Gson().fromJson(modelString, Game::class.java)
        game = Game(punter = game.punter, punters = game.punters, mapModel = game.mapModel, settings = null)

        val moves: Array<Move> = arrayOf(
            Splurge(0, arrayOf(0, 1, 2))
        )
        val updatedGame = applyMoves(moves, game)
        updatedGame.ownedRivers `should contain` River(0,1)
        updatedGame.ownedRivers `should contain` River(1,2)
        updatedGame.ownedRivers `should not contain` River(0,2)

    }

    @Test
    fun testApplyOptions() {
        val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        var game = Gson().fromJson(modelString, Game::class.java)
        game = Game(punter = game.punter, punters = game.punters, mapModel = game.mapModel, settings = null)

        // Another putner
        val moves: Array<Move> = arrayOf(
            Option(1, 0, 1)
        )
        val updatedGame = applyMoves(moves, game)
        updatedGame.unownedRivers `should not contain` River(0,1)

    }

    @Test
    fun testApplyOptionsOnMyRiver() {
        val modelString = """{"punter":0,"punters":2,"map":{"sites":[{"id":4,"x":2.0,"y":-2.0},{"id":1,"x":1.0,"y":0.0},{"id":3,"x":2.0,"y":-1.0},{"id":6,"x":0.0,"y":-2.0},{"id":5,"x":1.0,"y":-2.0},{"id":0,"x":0.0,"y":0.0},{"id":7,"x":0.0,"y":-1.0},{"id":2,"x":2.0,"y":0.0}],"rivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"mines":[1,5]}}"""
        var game = Gson().fromJson(modelString, Game::class.java)
        game = Game(punter = game.punter, punters = game.punters, mapModel = game.mapModel, settings = null)

        // Another putner
        val moves: Array<Move> = arrayOf(
            Claim(0, 0, 1),
            Option(1, 0, 1)
        )
        val updatedGame = applyMoves(moves, game)
        updatedGame.myRivers `should contain` River(0,1)
        updatedGame.unownedRivers `should not contain` River(0,1)
        updatedGame.ownedRivers `should contain` River(0,1)

    }



}

