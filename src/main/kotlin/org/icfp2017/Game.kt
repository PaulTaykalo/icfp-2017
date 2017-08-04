@file:Suppress("ArrayInDataClass")

package org.icfp2017
import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("punter") val punter: PunterID,
    @SerializedName("punters") val punters: Int,
    @SerializedName("map") val map: Map
)

data class Site(@SerializedName("id") val id: SiteID)
data class River(@SerializedName("source") val source: SiteID, @SerializedName("target") val target: SiteID, @SerializedName("owner") var owner: PunterID?)
data class Map(@SerializedName("punters") val sites: Array<Site>, @SerializedName("rivers") val rivers: Array<River>, @SerializedName("mines") val mines: Array<Int>)
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

fun Map.apply(moves: Array<Move>) {
    moves.forEach { move ->
        if (move !is Claim) return@forEach

        val river = rivers.find {
            move.source == it.source && move.target == it.target
        }

        if (river != null) river.owner = move.punter
    }

    private fun mostConnectedRivers(rivers: List<River>) : List<River>{
        val edge = findMostAdjacentEdgeInSpanningTree(map)
        return rivers.filter { (it.source == edge.v && it.target == edge.w)|| ((it.source == edge.w && it.target == edge.v)) }
    }
}

val Array<River>.unclaimed: List<River>
    get() = filter { it.owner == null }


fun Game.pass(): Pass {
    return Pass(punter)
}

fun Game.claim(river: River): Claim {
    return Claim(punter, river.source, river.target)
}

fun Game.claim(river: River?): Move = if (river == null) pass() else claim(river)