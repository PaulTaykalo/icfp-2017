package org.icfp2017.solver

import org.icfp2017.*

inline fun <T, R: Comparable<R>> Collection<T>.takeMaxBy(comparator: (T) -> R): Collection<T> {
    var maxValue: R? = null
    var maxItems = listOf<T>()

    forEach {
        val itValue = comparator(it)
        val currentValue = maxValue
        if (currentValue == null || itValue > currentValue)  {
            maxValue = itValue
            maxItems = listOf(it)
        } else {
            maxItems += it
        }
    }

    return maxItems
}

object DumbAndGreedy : Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)


        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }

        val currentScore = calculateScoreForReachable(game.sitesReachedForMine,game.siteScores)

        val costlyRivers = niceRivers.takeMaxBy {
            val newReachability = updateSitesReachability(game.sitesReachedForMine, it,game.myRivers, game.riversForSite)
            val newScore = calculateScoreForReachable(newReachability,game.siteScores)

            val mineBonus = if (it.target in game.mines || it.source in game.mines) 10 else 0
            newScore - currentScore + mineBonus
        }

        return Pair(game.claim(costlyRivers.firstOrNull()), game)
    }
}