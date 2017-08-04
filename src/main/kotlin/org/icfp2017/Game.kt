package org.icfp2017

import org.icfp2017.graph.findMostAdjacentEdgeInSpanningTree


sealed class Move
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID): Move()
data class Pass(val punter: PunterID): Move()

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

data class Site(val id: SiteID)
data class River(val source: SiteID, val target: SiteID, var owner: PunterID?)
class Map(val sites: Array<Site>, val rivers: Array<River>, val mines: Array<Int>)

class Game(val punter: PunterID, val punters: Int, val map: Map) {

    fun move(moves: Array<Move>): Move {
        apply(moves)
        val river = selectRiver()

        if (river != null) return Claim(punter, river.source, river.target)
        else return Pass(punter)
    }

    private fun selectRiver(): River? {
        val freeRivers = map.rivers.filter { it.owner == null }

        val river = mostConnectedRivers(freeRivers).firstOrNull()
        return river
    }

    private fun apply(moves: Array<Move>) {
        moves.forEach { move ->
            if (move !is Claim) return@forEach

            val river = map.rivers.find {
                move.source == it.source && move.target == it.target
            }

            if (river != null) river.owner = move.punter
        }
    }

    private fun mostConnectedRivers(rivers: List<River>) : List<River>{
        val edge = findMostAdjacentEdgeInSpanningTree(map)
        return rivers.filter { (it.source == edge.v && it.target == edge.w)|| ((it.source == edge.w && it.target == edge.v)) }
    }
}

