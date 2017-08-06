package org.icfp2017.solver

import org.icfp2017.*
import java.util.*


object RandomFree : Strategy<StrategyStateWithGame> {
    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    val random = Random()
    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val newGame = applyMoves(moves, state.game)
        val rivers = newGame.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(newGame.pass(), state)

        return Pair(newGame.claim(rivers[random.nextInt(rivers.size)]), StrategyStateWithGame(newGame))
    }
}