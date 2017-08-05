package org.icfp2017.solver

import org.icfp2017.*
import java.util.*


object RandomFree: Strategy<StrategyStateWithGame> {
    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    val random = Random()
    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return game.pass()

        return game.claim(rivers[random.nextInt(rivers.size)])
    }
}