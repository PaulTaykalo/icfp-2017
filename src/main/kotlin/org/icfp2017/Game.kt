@file:Suppress("ArrayInDataClass")

package org.icfp2017

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.icfp2017.server.SettingsResponse

private typealias MineID = Int
typealias Reachability = Map<MineID, Set<SiteID>>
private typealias ScoreFromMine = Map<SiteID, Map<MineID, Long>>
typealias RiversForSite = Map<SiteID, Set<River>>
typealias SitesForSite = Map<SiteID, Set<SiteID>>

fun calculateRiversForSites(map: MapModel): Map<SiteID, Set<River>> {

    val results = map.sites.map { it.id to mutableSetOf<River>() }.toMap()
    map.rivers.forEach {
        results[it.source]!!.add(it)
        results[it.target]!!.add(it)
    }

    return results
}

fun calculateSitesForSite(sites: Array<SiteModel>, riversForSite : RiversForSite): Map<SiteID, Set<SiteID>> {
    fun sitesForSite(site: SiteID): Set<SiteID> {
        val rivers = riversForSite[site]!!
        return setOf<SiteID>() + rivers.map { it.source } + rivers.map { it.target } - site
    }

    return sites.map { it.id to sitesForSite(it.id) }.toMap()
}

fun calculateScores(sites: Array<SiteModel>, mines:Array<SiteID>, sitesForSite: SitesForSite): ScoreFromMine {
    val scores =sites.map { it.id to mutableMapOf<SiteID, Long>() }.toMap()

    mines.forEach { mine ->
        var step = 1L
        scores[mine]!![mine] = 0
        var front = setOf(mine)
        while (front.isNotEmpty()) {
            front = front.flatMap { site ->
                val siteResults = sitesForSite[site]!!.filter { scores[it]!![mine] == null }
                siteResults.forEach { scores[it]!![mine] = step * step }
                siteResults
            }.toSet()
            step += 1
        }
    }

    return scores
}

val Game.currentScore: Long get() = calculateScoreForReachable(sitesReachedForMine, siteScores)

fun calculateScoreForReachable(sitesReachableFromMine: Reachability, scores: ScoreFromMine): Long {
    var sum = 0L
    sitesReachableFromMine.forEach { (mine, sites) ->
        sites.forEach { site ->
            sum += scores[site]!![mine]!!
        }
    }

    return sum
}

val Game.availableRivers: Set<River> get() {
    if (settings?.options == false) return unownedRivers
    if (availableOptions <= 0) return  unownedRivers
    return ownedRivers + unownedRivers - myRivers
}

fun expandReachableSitesForMineAndRiver(mine: SiteID, sites: Set<SiteID>, river: River, riversForSite: RiversForSite, myRivers: Set<River>): Set<SiteID> {
    // Sites that we will reach with this river
    val newSites = mutableSetOf<SiteID>()

    // If river is not reachable from current graph - return unchanged
    if (river.target !in (sites + mine) && river.source !in (sites + mine)) return sites

    val front = mutableSetOf(river.target, river.source)
    front.removeAll(sites)

    while (front.isNotEmpty()) {
        val site = front.first()
        newSites.add(site)
        front.remove(site)

        // All rivers that we can reach from this site
        val possibleRivers = riversForSite[site]!!
        val connectedRivers = possibleRivers.intersect(myRivers)

        val connectedSites = connectedRivers.map { it.source }.toSet() +
                connectedRivers.map { it.target }.toSet() - sites - newSites

        front.addAll(connectedSites)
    }

    return sites.plus(newSites)
}


fun updateSitesReachability(current: Reachability, river: River,myRivers:Set<River>, riversForSite:RiversForSite): Reachability {
    return current.mapValues { expandReachableSitesForMineAndRiver(it.key, it.value, river,  riversForSite ,myRivers) }
}

val Splurge.claims: List<Claim> get() = route
        .zip(route.drop(1))
        .map { (source, target) ->
            Claim(punter, source, target)
        }

// TODO :
fun applyMoves(moves: Array<Move>, game:Game):Game {

    // Apply moves to map.
    var newUnownedRivers = game.unownedRivers
    var newOwnedRivers = game.ownedRivers
    var newMyRivers = game.myRivers
    var newSiteReachebleForMine = game.sitesReachedForMine

    var newAvailableOptionals = game.availableOptions
    var newOptionalRivers = game.optionalRivers


    val claims = moves.flatMap {
        when(it) {
            is Pass -> listOf()
            is Splurge -> it.claims
            is Claim -> listOf(it)
            is Option -> {
                newOptionalRivers += River(it.source, it.target);
                if (it.punter == game.punter) newAvailableOptionals -= 1
                listOf(Claim(it.punter, it.source, it.target))
            }
        }
    }

    claims.forEach {
        val river = River(it.source, it.target)
        newUnownedRivers -= river
        newOwnedRivers += river

        if (it.punter == game.punter) {
            newMyRivers += river
            newSiteReachebleForMine = updateSitesReachability(newSiteReachebleForMine, river, newMyRivers, game.riversForSite)
        }
    }

    return Game(
            punter = game.punter,
            punters = game.punters,
            settings = game.settings,
            sites = game.sites,
            mines = game.mines,
            ownedRivers = newOwnedRivers,
            unownedRivers = newUnownedRivers,
            myRivers = newMyRivers,
            sitesReachedForMine = newSiteReachebleForMine,
            riversForSite = game.riversForSite,
            sitesForSite = game.sitesForSite,
            siteScores = game.siteScores,
            availableOptions = newAvailableOptionals,
            optionalRivers = newOptionalRivers)

}

data class Game(
    val punter: PunterID,
    val punters: Int,
    val settings: SettingsResponse? = null,
    val sites : Array<SiteModel>,
    val mines:Set<SiteID>,
    val availableOptions: Int,
    val optionalRivers: Set<River>,
    val ownedRivers:Set<River> = setOf<River>(),
    val unownedRivers:Set<River>,
    val myRivers:Set<River> = setOf<River>(),
    val sitesReachedForMine:Reachability = mines.map { it to setOf<SiteID>()}.toMap(),
    val riversForSite:RiversForSite,
    val sitesForSite:SitesForSite,
    val siteScores:ScoreFromMine

) {
    companion object {
        fun create(
            punter: PunterID,
            punters: Int,
            mapModel: MapModel,
            settings: SettingsResponse?
        ): Game {
            val riversForSite = calculateRiversForSites(mapModel)
            val sites = mapModel.sites
            val sitesForSite = calculateSitesForSite(sites, riversForSite)
            return Game(punter, punters, settings,
                    sites = sites,
                    mines = mapModel.mines.toSet(),
                    unownedRivers = mapModel.rivers.toSet(),
                    riversForSite = riversForSite,
                    sitesForSite = sitesForSite,
                    siteScores = calculateScores(sites, mapModel.mines, sitesForSite),
                    availableOptions = mapModel.mines.size,
                    optionalRivers = setOf()
            )
        }

    }
}
data class SiteModel(val id: SiteID)
data class River(val source: SiteID, val target:SiteID) {
    override fun hashCode(): Int {
        return source.hashCode() xor target.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is River) { return false }
        return other.hashCode() == hashCode()
    }
}


//typealias River = Array<SiteID>
//
//val River.source: SiteID get() = get(0)
//val River.target: SiteID get() = get(1)

data class MapModel(val sites: Array<SiteModel>, val rivers: Array<River>, val mines: Array<SiteID>)

sealed class Move
data class Claim(val punter: PunterID, val source: SiteID, val target: SiteID) : Move()
data class Pass(val punter: PunterID) : Move()
data class Splurge(val punter: PunterID, val route: Array<SiteID>) : Move()
data class Option(val punter: PunterID, val source: SiteID, val target: SiteID) : Move()

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

fun Game.pass(): Pass {
    return Pass(punter)
}

fun Game.claim(river: River?): Move {
    if (river == null) return pass()
    if (river in unownedRivers) return Claim(punter, river.source, river.target)
    else return Option(punter, river.source, river.target)
}