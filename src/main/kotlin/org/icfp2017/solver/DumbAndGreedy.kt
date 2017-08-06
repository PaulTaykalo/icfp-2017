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

    override fun serverMove(moves: Array<Move>, state: Game): Pair<Move, Game> {
        state.apply(moves)

        val reachedPoints = state.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + state.mines.toSet()

        val niceRivers = nicePoints
                .flatMap { state.riversForSite[it]!! }
                .filter { it in state.unownedRivers }

        val currentScore = state.calculateScoreForReachable(state.sitesReachedForMine)


        val costlyRivers = niceRivers.takeMaxBy {
            val newReachability = state.updateSitesReachability(state.sitesReachedForMine, it)
            val newScore = state.calculateScoreForReachable(newReachability)

            newScore - currentScore
        }

        val minesFriendlyRivers = costlyRivers.takeMaxBy {
            val newPoint = if(it.source in nicePoints) it.target else it.source
            0 - state.siteScores[newPoint]!!.values.sum()
        }

        return Pair(state.claim(minesFriendlyRivers.firstOrNull()), state)
    }
}