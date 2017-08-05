package org.icfp2017.solver

import org.icfp2017.*

object DumbAndGreedy : Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun move(moves: Array<Move>, state: StrategyStateWithGame): Move {
        val game = state.game
        game.apply(moves)
        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines.toSet()
        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }

        val currentScore = game.calculateScoreForReachable(game.sitesReachedForMine)
        val river = niceRivers.maxBy {
            val newReachability = game.updateSitesReachability(game.sitesReachedForMine, it)
            val newScore = game.calculateScoreForReachable(newReachability)
            val delta = newScore - currentScore

            if (it.target in game.mines || it.source in game.mines) delta + 10

            delta
        }

        return game.claim(river)
    }
}