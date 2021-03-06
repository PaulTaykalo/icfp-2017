package org.icfp2017.solver

import org.icfp2017.*

object GreedySharpLover : Strategy<Game> {
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
            if (minesForSource.isEmpty() || minesForTarget.isEmpty()) {
                val targetScore = game.siteScores[currentRiver.target]!!
                        .filterNot { it.key in minesForSource + currentRiver.source + currentRiver.target }
                        .values.min()?.toInt()
                val sourceScore = game.siteScores[currentRiver.source]!!
                        .filterNot { it.key in minesForTarget + currentRiver.source + currentRiver.target }
                        .values.min()?.toInt()

                if (targetScore != null && sourceScore != null) {
                    if (targetScore == 0) {
                        return@takeMinBy sourceScore!!
                    } else if (sourceScore == 0) {
                        return@takeMinBy targetScore!!
                    }
                    return@takeMinBy arrayOf(targetScore, sourceScore).min()!!
                }

                return@takeMinBy sourceScore ?: targetScore ?: Int.MAX_VALUE
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
