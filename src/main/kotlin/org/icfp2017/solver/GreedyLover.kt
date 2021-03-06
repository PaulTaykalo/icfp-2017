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

        val costlyRivers = niceRivers.takeMinBy { currentRiver ->
            val minesForSource = game.sitesReachedForMine.filter { currentRiver.source in it.value }.keys
            val minesForTarget = game.sitesReachedForMine.filter { currentRiver.target in it.value }.keys

            fun scoresForSite(site: SiteID): Int {
                val siteScore =
                        game.siteScores[site]!!
                                .filterNot { it.key in minesForTarget }
                                .values.min()?.toInt()

                return siteScore ?: Int.MAX_VALUE
            }

            // Claim first point
            if (minesForSource.isEmpty() && minesForTarget.isEmpty()) {
                if (currentRiver.source in game.mines) return@takeMinBy scoresForSite(currentRiver.target)
                else return@takeMinBy scoresForSite(currentRiver.source)
            }

            if (minesForSource.isEmpty()) {
                val sourceScore = game.siteScores[currentRiver.source]!!
                        .filterNot { it.key in minesForTarget }
                        .values.min()
                return@takeMinBy sourceScore?.toInt() ?: Int.MAX_VALUE
            }

            if (minesForTarget.isEmpty()) {
                val targetScore = game.siteScores[currentRiver.target]!!
                        .filterNot { it.key in minesForSource }
                        .values.min()
                return@takeMinBy targetScore?.toInt() ?: Int.MAX_VALUE
            }

            // Join graphs
            if (minesForSource.intersect(minesForTarget).count() == 0) {
                return@takeMinBy Int.MIN_VALUE
            }

            Int.MAX_VALUE
        }

        return Pair(game.claim(costlyRivers.firstOrNull()), game)
    }
}


object GreedyLover2 : Strategy<Game> {
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

            // Claim new point
            if (minesForSource.isEmpty()) {
                val sourceScore = game.siteScores[currentRiver.source]!!
                    .filterNot { it.key in minesForTarget }
                    .values.sorted().take(5).map{ Math.sqrt(it.toDouble()).toLong() }.sum()
                // first n moves
                return@takeMinBy sourceScore?.toInt() ?: Int.MAX_VALUE
            }

            if (minesForTarget.isEmpty()) {
                val targetScore = game.siteScores[currentRiver.target]!!
                    .filterNot { it.key in minesForSource }
                    .values.sorted().take(5).map{ Math.sqrt(it.toDouble()).toLong() }.sum()
                return@takeMinBy targetScore?.toInt() ?: Int.MAX_VALUE
            }

            // Join graphs
            if (minesForSource.intersect(minesForTarget).count() == 0) {
                return@takeMinBy Int.MIN_VALUE
            }

            Int.MAX_VALUE
        }

        return Pair(game.claim(costlyRivers.firstOrNull()), game)
    }
}
