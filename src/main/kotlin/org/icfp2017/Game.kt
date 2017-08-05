@file:Suppress("ArrayInDataClass")

package org.icfp2017

import com.google.gson.Gson

private typealias Reachability = Map<SiteID, Set<SiteID>>

class Game(
        val punter: PunterID,
        val punters: Int,
        map: MapModel
) {
    var ownedRivers = setOf<River>()
    var unownedRivers = map.rivers.toSet()
    var myRivers = setOf<River>()
    var mines = map.mines.toSet()

    var sitesReachedForMine = mines.map { it to setOf<SiteID>()}.toMap()

    val riversForSite = calculateRiversForSites(map)
    val sitesForSite = calculateSitesForSite(map)
    val siteScores = calculateScores(map)

    private fun calculateRiversForSites(map: MapModel): Map<SiteID, Set<River>> {
        val results = map.sites.map { it.id to mutableSetOf<River>() }.toMap()
        map.rivers.forEach {
            results[it.source]!!.add(it)
            results[it.target]!!.add(it)
        }

        return results
    }

    private fun calculateSitesForSite(map: MapModel): Map<SiteID, Set<SiteID>> {
        fun sitesForSite(site: SiteID): Set<SiteID> {
            val rivers = riversForSite[site]!!
            return setOf<SiteID>() + rivers.map { it.source } + rivers.map { it.target } - site
        }

        return map.sites.map { it.id to sitesForSite(it.id) }.toMap()
    }


    private fun calculateScores(map:MapModel): Map<SiteID, Map<SiteID, Long>> {
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
            val river = River(move.source, move.target)
            unownedRivers -= river
            ownedRivers += river

            if (move.punter == punter) {
                myRivers += river
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

                val rivers = riversForSite[site]!! - myRivers
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

data class SiteModel(val id: SiteID)
data class River(val source: SiteID, val target:SiteID)

//typealias River = Array<SiteID>
//
//val River.source: SiteID get() = get(0)
//val River.target: SiteID get() = get(1)

data class MapModel(val sites: Array<SiteModel>, val rivers: Array<River>, val mines: Array<SiteID>)

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