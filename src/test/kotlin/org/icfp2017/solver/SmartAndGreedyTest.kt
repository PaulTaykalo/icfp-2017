package org.icfp2017.solver

import com.google.gson.Gson
import org.amshove.kluent.`should be in`
import org.amshove.kluent.`should be`
import org.icfp2017.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

val lambdaMap: MapModel = Gson().fromJson(
        File(SmartAndGreedyTest::class.java.getResource("lambda.json").path).readText(),
        MapModel::class.java)

val sampleMap: MapModel = Gson().fromJson(
        File(SmartAndGreedyTest::class.java.getResource("sample.json").path).readText(),
        MapModel::class.java)

class SmartAndGreedyTest {

    @Test internal fun firstMoveTest() {
        var game = Game(1, 2, mapModel = sampleMap)

        fun move(moves: Array<Move> = arrayOf()): Move {
            val result = SmartAndGreedy.serverMove(
                    moves = arrayOf(Pass(0), Pass(0)),
                    state = game
            )

            game = result.second
            return result.first
        }

        move() `should be in` setOf(
                Claim(1, 1,3),
                Claim(1, 1,7),
                Claim(1,3,5),
                Claim(1,5,7))
    }
}

class DumbAndGreedy2Test {
    @Test internal fun firstMoveTest() {
        var game = Game(0, 2, mapModel = lambdaMap)

        fun move(moves: Array<Move> = arrayOf()): Move {
            val result = SmartAndGreedy.serverMove(moves, game)

            game = result.second
            return result.first
        }

        move(arrayOf(
                Claim(0, 23,27),
                Claim(1, 26,27),
                Claim(0 , 26,29),
                Claim(1, 23,35)
        ))

        move() `should be in` setOf(
                Claim(1, 1,3),
                Claim(1, 1,7),
                Claim(1,3,5),
                Claim(1,5,7))
    }
}