package org.icfp2017.solver

import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.applyMoves
import org.icfp2017.claim

object FirstFree: Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val newGame = applyMoves(moves, state.game)
        return Pair(newGame.claim(newGame.unownedRivers.firstOrNull()), StrategyStateWithGame(newGame))
    }
}