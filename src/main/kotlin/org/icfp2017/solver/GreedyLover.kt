package org.icfp2017.solver

import org.icfp2017.*

object GreedyLover : Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)


        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }

        val currentScore = calculateScoreForReachable(game.sitesReachedForMine, game.siteScores)

        val costlyRivers = niceRivers.takeMaxBy { currentRiver ->
            val minesForSource = game.sitesReachedForMine.filter { currentRiver.source in it.value }.keys
            val minesForTarget = game.sitesReachedForMine.filter { currentRiver.target in it.value }.keys

            if (minesForSource.intersect(minesForTarget).count() == 0) {
                return@takeMaxBy Int.MAX_VALUE
            }





            // standard greedy
            val newReachability = updateSitesReachability(game.sitesReachedForMine, currentRiver, game.myRivers, game.riversForSite)
            val newScore = calculateScoreForReachable(newReachability, game.siteScores)

            val mineBonus = if (currentRiver.target in game.mines || currentRiver.source in game.mines) 10 else 0
            (newScore - currentScore + mineBonus).toInt()
        }

        return Pair(game.claim(costlyRivers.firstOrNull()), game)
    }
}
