package org.icfp2017.solver

import org.amshove.kluent.`should be in`
import org.amshove.kluent.`should be`
import org.icfp2017.*
import org.junit.Assert.*
import org.junit.Test

val lambdaMap = MapModel(
        sites = arrayOf(
                SiteModel(0),
                SiteModel(1),
                SiteModel(2),
                SiteModel(3),
                SiteModel(4),
                SiteModel(5),
                SiteModel(6),
                SiteModel(7)),
        rivers = arrayOf(
                River(0, 1),
                River(0, 7),
                River(1, 7),
                River(1, 2),
                River(1, 3),
                River(2, 3),
                River(3, 4),
                River(3, 5),
                River(4, 5),
                River(5, 6),
                River(5, 7),
                River(6, 7)),
        mines = arrayOf(1, 5)
)

class SmartAndGreedyTest {
    @Test
    internal fun firstMoveTest() {
        var game = Game(1, 2, mapModel = lambdaMap)

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