package org.icfp2017.solver

import org.icfp2017.*

object DumbAndGreedy : Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, state: Game): Pair<Move, Game> {
        state.apply(moves)

        val reachedPoints = state.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + state.mines.toSet()

        val niceRivers = nicePoints
                .flatMap { state.riversForSite[it]!! }
                .filter { it in state.unownedRivers }

        val currentScore = state.calculateScoreForReachable(state.sitesReachedForMine)

        val river = niceRivers.maxBy {
            val newReachability = Logger.measurePart("dump: update reachable") {
                state.updateSitesReachability(state.sitesReachedForMine, it)
            }
            val newScore = Logger.measurePart("dump: scores") {
                state.calculateScoreForReachable(newReachability)
            }

            val delta = newScore - currentScore

            if (it.target in state.mines || it.source in state.mines) delta + 10

            delta
        }

        Logger.measureDone("dump: update reachable")
        Logger.measureDone("dump: scores")

        return Pair(state.claim(river), state)
    }
}