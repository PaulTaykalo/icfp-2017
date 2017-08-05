package org.icfp2017.solver

import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.claim

object FirstFree: Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        return game.claim(game.unownedRivers.firstOrNull())
    }
}