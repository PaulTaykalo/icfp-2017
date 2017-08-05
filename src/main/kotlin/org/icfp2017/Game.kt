@file:Suppress("ArrayInDataClass")

package org.icfp2017

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private typealias Reachability = Map<SiteID, Set<SiteID>>

data class Game(
        val punter: PunterID,
        val punters: Int,
        val map: MapModel
) {
    var unownedRivers = map.rivers.toSet()
    var mines = map.mines.toSet()

    var sitesReachedForMine = mines.map { it to setOf<SiteID>()}.toMap()

    val riversForSite = calculateRiversForSites()
    val sitesForSite = calculateSitesForSite()
    val siteScores = calculateScores()

    private fun calculateRiversForSites(): Map<SiteID, Set<River>> {
        val results = mapOf(*map.sites.map { it.id to mutableSetOf<River>() }.toTypedArray())
        map.rivers.forEach {
            results[it.source]!!.add(it)
            results[it.target]!!.add(it)
        }

        return results
    }

    private fun calculateSitesForSite(): Map<SiteID, Set<SiteID>> {
        fun sitesForSite(site: SiteID): Set<SiteID> {
            val rivers = riversForSite[site]!!
            return setOf<SiteID>() + rivers.map { it.source } + rivers.map { it.target } - site
        }

        return map.sites.map { it.id to sitesForSite(it.id) }.toMap()
    }


    private fun calculateScores(): Map<SiteID, Map<SiteID, Long>> {
        var scores = map.sites.map { it.id to mutableMapOf<SiteID, Long>() }.toMap()

        mines.forEach { mine ->
            var step = 1L
            scores[mine]!![mine] = 0
            var front = mutableSetOf(mine)
            while (front.isNotEmpty()) {
                val site = front.elementAt(0)
                val sites = sitesForSite[site]!!.filter { scores[it]!![mine] == null }
                sites.forEach { scores[it]!![mine] = step * step }

                front.addAll(sites)
                front.remove(site)
                step += 1
            }
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

            if (river.owner == punter) {
                sitesReachedForMine = updateSitesReachability(sitesReachedForMine, river)
            }
        }
    }

    fun updateSitesReachability(current: Reachability, river: River): Reachability {
        return current.mapValues { updateSitesReachability(it.key, it.value, river) }
    }

    private fun updateSitesReachability(mine: SiteID, sites: Set<SiteID>, river: River): Set<SiteID> {
        val newSites = mutableSetOf<SiteID>()

        if (river.target in (sites + mine) || river.source in (sites + mine)) {
            val front = mutableSetOf(river.target, river.source)
            front.removeAll(sites)

            while (front.isNotEmpty()) {
                val site = front.first()
                newSites.add(site)
                front.remove(site)

                val rivers = riversForSite[site]!!.filter { it.owner == punter }
                val connectedSites = rivers.map { it.source }.toSet() +
                        rivers.map { it.target }.toSet() - sites - newSites

                front.addAll(connectedSites)
            }
        }

        return sites.plus(newSites)
    }

    fun calculateScoreForReachable(sitesReachableFromMine: Reachability): Long {
        var sum = 0L
        sitesReachableFromMine.forEach { (mine, sites) ->
            sites.forEach { site ->
                sum += siteScores[site]!![mine]!!
            }
        }

        return sum
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
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID) : Move()
data class Pass(val punter: PunterID) : Move()

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