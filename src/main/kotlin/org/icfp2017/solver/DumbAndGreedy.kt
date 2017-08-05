package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.server.ServerMove
import sun.rmi.runtime.Log

object DumbAndGreedy : Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val game = state.game
        game.apply(moves)

        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines.toSet()

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }

        val currentScore = game.calculateScoreForReachable(game.sitesReachedForMine)


        val river = niceRivers.maxBy {
            val newReachability = Logger.measurePart("dump: update reachable") {
                game.updateSitesReachability(game.sitesReachedForMine, it)
            }
            val newScore = Logger.measurePart("dump: scores") {
                game.calculateScoreForReachable(newReachability)
            }

            val delta = newScore - currentScore

            if (it.target in game.mines || it.source in game.mines) delta + 10

            delta
        }

        Logger.measureDone("dump: update reachable")
        Logger.measureDone("dump: scores")

        return Pair(game.claim(river), state)
    }
}