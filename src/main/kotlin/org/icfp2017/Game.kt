@file:Suppress("ArrayInDataClass")

package org.icfp2017
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
data class Game(
        val punter: PunterID,
        val punters: Int,
        val map: MapModel
) {
    var unownedRivers: Set<River> = map.rivers.toSet()
    var mines: Set<SiteID> = map.mines.toSet()
    var reachableSites: Set<SiteID> = setOf()

    val riversForSite = calculateRiversForSites()
    val sitesForSite = calculateSitesForSite()
    val siteScores = calculateScores()

    private fun  calculateSitesForSite(): Map<SiteID, Set<SiteID>> {
        fun sitesForSite(site: SiteID): Set<SiteID> {
            val rivers = riversForSite[site]!!
            return setOf<SiteID>() + rivers.map { it.source } + rivers.map { it.target } - site
        }

        return map.sites.map { it.id to sitesForSite(it.id)}.toMap()
    }

    private fun calculateRiversForSites(): Map<SiteID, Set<River>> {
        val results = mapOf(*map.sites.map { it.id to mutableSetOf<River>()}.toTypedArray())
        map.rivers.forEach {
            results[it.source]!!.add(it)
            results[it.target]!!.add(it)
        }

        return results
    }

    private fun calculateScores(): Map<SiteID, Map<SiteID, Long>> {
        var scores = mapOf(*map.sites.map { it.id to mutableMapOf<SiteID, Long>() }.toTypedArray())

        var step = 1L
        mines.forEach { mine ->
            val rivers = riversForSite[mine]!!

            rivers
                    .flatMap { arrayListOf(it.source, it.target) }
                    .filter { scores[it]!![mine] == null }
                    .forEach { scores[it]!![mine] = step * step }
            step += 1
        }

        Logger.log(Gson().toJson(scores))
        return scores
    }

    fun apply(moves: Array<Move>) {
        // Apply moves to map.
        moves.forEach { move ->
            if (move !is Claim) return@forEach
            val river = map.rivers.find {
                move.source == it.source && move.target == it.target
            } ?: return@forEach

            river.owner = move.punter
            unownedRivers -= river

            if (move.punter == punter) {
                reachableSites += move.source
                reachableSites += move.target
            }
        }
    }
}

data class Site(
        @SerializedName("id") val id: SiteID
)

data class River(
        @SerializedName("source") val source: SiteID,
        @SerializedName("target") val target: SiteID,
        @SerializedName("owner") var owner: PunterID?
)

data class MapModel(
        @SerializedName("sites") val sites: Array<Site>,
        @SerializedName("rivers") val rivers: Array<River>,
        @SerializedName("mines") val mines: Array<Int>
)


sealed class Move
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID): Move()
data class Pass(val punter: PunterID): Move()

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

fun Game.pass(): Pass {
    return Pass(punter)
}

fun Game.claim(river: River): Claim {
    return Claim(punter, river.source, river.target)
}

fun Game.claim(river: River?): Move = if (river == null) pass() else claim(river)