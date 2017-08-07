package org.icfp2017.solver

import org.icfp2017.*

data class CollectionComparator<T: Comparable<T>>(val values: Collection<T>): Comparable<CollectionComparator<T>> {
    override fun compareTo(other: CollectionComparator<T>): Int {
        (values zip other.values).forEach {
            if (it.first != it.second) return it.first.compareTo(it.second)
        }

        return 0
    }
}

object GreedyDeepLover: Strategy<Game> {
    override fun prepare(game: Game) = game

    override fun serverMove(moves: Array<Move>, oldState: Game): Pair<Move, Game> {
        val game = applyMoves(moves, oldState)


        val reachedPoints = game.sitesReachedForMine.flatMap { it.value }
        val nicePoints = reachedPoints.toSet() + game.mines

        val niceRivers = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }

        val costlyRivers = niceRivers.takeMinBy { currentRiver ->
            val minesForSource = game.sitesReachedForMine.filter { currentRiver.source in it.value }.keys
            val minesForTarget = game.sitesReachedForMine.filter { currentRiver.target in it.value }.keys

            fun scoresForSite(site: SiteID): CollectionComparator<Long> {
                val sourceNewMines =
                        game.siteScores[site]!!
                                .filterNot { it.key in minesForTarget }
                                .values.sorted()

                if (sourceNewMines.isEmpty()) return CollectionComparator(listOf(Long.MAX_VALUE))

                return CollectionComparator(sourceNewMines)
            }

            // Claim new point
            if (minesForSource.isEmpty()) return@takeMinBy scoresForSite(currentRiver.source)
            if (minesForTarget.isEmpty()) return@takeMinBy scoresForSite(currentRiver.target)

            // Join graphs
            if (minesForSource.intersect(minesForTarget).count() == 0) {
                return@takeMinBy CollectionComparator(listOf(Long.MIN_VALUE))
            }

            CollectionComparator(listOf(Long.MAX_VALUE))
        }

        return Pair(game.claim(costlyRivers.firstOrNull()), game)
    }
}