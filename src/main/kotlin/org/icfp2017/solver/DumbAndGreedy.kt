package org.icfp2017.solver

import org.icfp2017.*

object DumbAndGreedy: Strategy {

    val reachableSites: MutableSet<SiteID> = mutableSetOf()

    override fun move(game: Game): Move {
        val river = game.map.rivers
                .asSequence()
                .filter { it.owner == null }
                .filter {
                    val sourceReached = it.source in reachableSites
                    val targetReached = it.target in reachableSites

                    /// Avoid cycles in graph
                    if (sourceReached && targetReached) return@filter false

                    val sourceIsMine = it.source in game.map.mines
                    val targetIsMine = it.target in game.map.mines

                    return@filter sourceReached || targetReached || sourceIsMine || targetIsMine
                }
                // Add ranging of rivers.
                .firstOrNull() ?: return game.pass()

        reachableSites.add(river.target)
        reachableSites.add(river.source)

        return game.claim(river)
    }
}