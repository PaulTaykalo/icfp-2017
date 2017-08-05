package org.icfp2017.solver

import org.icfp2017.*

object DumbAndGreedy: Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        val rivers = game.unownedRivers
                .asSequence()
                .filter {
                    val sourceReached = it.source in game.reachableSites
                    val targetReached = it.target in game.reachableSites

                    /// Avoid cycles in graph
                    if (sourceReached && targetReached) return@filter false

                    val sourceIsMine = it.source in game.mines
                    val targetIsMine = it.target in game.mines

                    return@filter sourceReached || targetReached || sourceIsMine || targetIsMine
                }

        return game.claim(rivers.firstOrNull())
    }
}