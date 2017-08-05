package org.icfp2017.solver

import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.claim
import org.icfp2017.unclaimed

object FirstFree: Strategy {
    override fun move(game: Game): Move {
        return game.claim(game.map.rivers.unclaimed.firstOrNull())
    }
}