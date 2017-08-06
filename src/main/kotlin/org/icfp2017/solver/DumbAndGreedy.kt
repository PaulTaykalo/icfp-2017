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
        val nicePoints = reachedPoints.toSet() + state.mines

        val niceRivers = nicePoints
                .flatMap { state.riversForSite[it]!! }
                .filter { it in state.unownedRivers }

        val currentScore = state.calculateScoreForReachable(state.sitesReachedForMine)

        val costlyRivers = niceRivers.takeMaxBy {
            val newReachability = state.updateSitesReachability(state.sitesReachedForMine, it)
            val newScore = state.calculateScoreForReachable(newReachability)

            val mineBonus = if (it.target in state.mines || it.source in state.mines) 10 else 0
            newScore - currentScore + mineBonus
        }

        return Pair(state.claim(costlyRivers.firstOrNull()), state)
    }
}