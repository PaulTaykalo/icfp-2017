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

class BestMove: Strategy<Game> {
    override fun prepare(game: Game) = game
    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)

        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.availableRivers }.toSet()

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

object DumbAndGreedy : Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)


        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.availableRivers }

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

object DumbAndGreedy2 : Strategy<Game> {
    override fun prepare(game: Game) = game

    private fun topRivers(game: Game): Collection<Pair<River, Long>> {
        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.availableRivers }

        val currentScore = calculateScoreForReachable(game.sitesReachedForMine,game.siteScores)

        val costlyRivers = niceRivers
                .map {
                    val newReachability = updateSitesReachability(game.sitesReachedForMine, it, game.myRivers, game.riversForSite)
                    val newScore = calculateScoreForReachable(newReachability, game.siteScores)

                    val mineBonus = if (it.target in game.mines || it.source in game.mines) 10 else 0
                    it to newScore - currentScore + mineBonus
                }
                .takeMaxBy { it.second }

        return costlyRivers
    }

    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)

        val topRivers = topRivers(game)
        val top2rivers = topRivers.takeMaxBy { (river, score) ->
            val localGame = applyMoves(arrayOf(game.claim(river)), game)

            topRivers(localGame).map { it.second }.max() ?: 0
        }

        return Pair(game.claim(top2rivers.firstOrNull()?.first), game)
    }
}

object SmartAndGreedy: Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, state: Game): Pair<Move, Game> {
        val game = applyMoves(moves, state)

        // Selects less developed graph

        val graphs = game.sitesReachedForMine.toList().sortedBy { 0 - it.second.size  }
        val rivers = graphs.asSequence().map { (mine, sites) ->
            val nicePoints = sites + mine

            val niceRivers = nicePoints
                    .flatMap { game.riversForSite[it]!! }
                    .filter { it in game.availableRivers }

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
                val deltaScore = newScore - currentScore

                // Prefer mines
                val mineBoues = if (river.target in game.mines || river.source in game.mines) 5 else 0

                // Prefer points with many connections
                val rivers = (game.riversForSite[it]!!.intersect(game.availableRivers)).size

                // distance to other mines
                val score = game.siteScores[it]!!.filter { it.key != mine }.values.sum()

                val totalScore = deltaScore + rivers - score
                totalScore
            }

            pointsToRivers[targetPoints.firstOrNull()]
        }

        val river = rivers.firstOrNull() ?: game.unownedRivers.firstOrNull()

        return Pair(game.claim(river), game)
    }
}