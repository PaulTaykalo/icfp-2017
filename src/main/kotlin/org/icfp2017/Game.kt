package org.icfp2017

sealed class Move
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID): Move()
data class Pass(val punter: PunterID): Move()

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

data class Site(val id: SiteID)
data class River(val source: SiteID, val target: SiteID, var owner: PunterID?)
class Map(val sites: Array<Site>, val rivers: Array<River>, val mines: Array<Int>)

fun Map.apply(moves: Array<Move>) {
    moves.forEach { move ->
        if (move !is Claim) return@forEach

        val river = rivers.find {
            move.source == it.source && move.target == it.target
        }

        if (river != null) river.owner = move.punter
    }
}

class Game(
        val punter: PunterID,
        val punters: Int,
        val map: Map,
        val strategy: Strategy = FirstFree) {

    fun move(moves: Array<Move>): Move {
        map.apply(moves)
        return strategy.move(this)
    }
}

object FirstFree: Strategy {
    override fun move(game: Game): Move {
        val freeRivers = game.map.rivers.filter { it.owner == null }
        val river = freeRivers.firstOrNull()

        if (river != null) return  Claim(game.punter, river.source, river.target)
        else return  Pass(game.punter)
    }
}

interface Strategy {
    fun move(game: Game): Move
}

