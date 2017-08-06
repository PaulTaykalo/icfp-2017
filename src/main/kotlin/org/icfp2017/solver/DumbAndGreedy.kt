package org.icfp2017.solver

import org.icfp2017.*

object DumbAndGreedy : Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, game: Game): Pair<Move, Game> {
        val newGame = applyMoves(moves, game)

        val reachedPoints = newGame.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + newGame.mines.toSet()

        val niceRivers = nicePoints
                .flatMap { newGame.riversForSite[it]!! }
                .filter { it in newGame.unownedRivers }

        val currentScore = calculateScoreForReachable(newGame.sitesReachedForMine,newGame.siteScores)

        val river = niceRivers.maxBy {
            val newReachability = updateSitesReachability(newGame.sitesReachedForMine, it,newGame.myRivers,newGame.riversForSite)
            val newScore = calculateScoreForReachable(newReachability,newGame.siteScores)

            val delta = newScore - currentScore

            if (it.target in newGame.mines || it.source in newGame.mines) delta + 10

            delta
        }

        return Pair(newGame.claim(river), newGame)
    }
}