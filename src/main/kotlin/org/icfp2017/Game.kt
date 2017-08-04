package org.icfp2017

class Game(val punter: PunterID, val punters: Int, val map: Map)
class Map(val sites: Array<Site>, val rivers: Array<River>, val mines: Array<Int>)
data class River(val source: SiteID, val target: SiteID, var owner: PunterID?)

sealed class Move
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID): Move()
data class Pass(val punter: PunterID): Move()

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

data class Site(val id: SiteID)

fun Map.apply(moves: Array<Move>) {
    moves.forEach { move ->
        if (move !is Claim) return@forEach

        val river = rivers.find {
            move.source == it.source && move.target == it.target
        }

        if (river != null) river.owner = move.punter
    }
}

val Array<River>.unclaimed: List<River>
    get() = filter { it.owner == null }


