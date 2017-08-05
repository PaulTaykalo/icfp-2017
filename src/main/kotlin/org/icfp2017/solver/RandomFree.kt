package org.icfp2017.solver

import org.icfp2017.*
import java.util.*


object RandomFree: Strategy {
    val random = Random()
    override fun move(game: Game): Move {
        val rivers = game.unownedRivers.toList()
        if (rivers.isEmpty()) return game.pass()

        return game.claim(rivers[random.nextInt(rivers.size)])
    }
}