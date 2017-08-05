package org.icfp2017.solver

import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.claim
import org.icfp2017.server.ServerMove

object FirstFree: Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): ServerMove {
        val game = state.game
        game.apply(moves)
        return ServerMove(game.claim(game.unownedRivers.firstOrNull()), state)
    }
}