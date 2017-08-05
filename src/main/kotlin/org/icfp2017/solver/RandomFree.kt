package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.ServerMove
import java.util.*


object RandomFree : Strategy<StrategyStateWithGame> {
    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    val random = Random()
    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val game = state.game
        game.apply(moves)
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(game.pass(), state)

        return Pair(game.claim(rivers[random.nextInt(rivers.size)]), state)
    }
}