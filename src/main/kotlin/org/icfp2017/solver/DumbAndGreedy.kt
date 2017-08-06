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

object SmartAndGreedy: Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, state: Game): Pair<Move, Game> {
        val game = applyMoves(moves, state)

        // Selects less developed graph
        val (mine, sites) = state.sitesReachedForMine.maxBy { 0 - it.value.size } ?: return state.pass() to state
        val nicePoints = sites + state.mines

        val niceRivers = nicePoints
                .flatMap { state.riversForSite[it]!! }
                .filter { it in state.unownedRivers }

        val pointsToRivers = niceRivers.map {
            if (it.target in sites) it.source to it
            else it.target to it
        }.toMap()

        val currentScore = calculateScoreForReachable(game.sitesReachedForMine,game.siteScores)

        val newPoints = pointsToRivers.keys
        val targetPoints = newPoints.sortedBy {

            val river = pointsToRivers[it]!!

            // Expected new score
            val newReachability = updateSitesReachability(game.sitesReachedForMine, river,game.myRivers, game.riversForSite)
            val newScore = calculateScoreForReachable(newReachability,game.siteScores)
            val deltaScore = newScore - newScore

            // Prefer mines
            val mineBoues = if (river.target in state.mines || river.source in state.mines) 5 else 0

            // Prefer points with many connections
            val rivers = (state.unownedRivers - state.riversForSite[it]!!).size

            // distance to other mines
            val score = state.siteScores[it]!!.filter { it.key != mine }.values.sum()


            deltaScore + mineBoues + rivers - score
        }

        return Pair(state.claim(pointsToRivers[targetPoints.firstOrNull()]), state)
    }
}