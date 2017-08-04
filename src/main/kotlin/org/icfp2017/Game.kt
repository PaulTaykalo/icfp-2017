package org.icfp2017

sealed class Move {
    class Claim(val punter: PunterID, source: SiteID, target: SiteID): Move()
    class Pass(val punter: PunterID)
}

typealias SiteID = Int
typealias PunterID = Int
typealias PunterName = String

class Site(val id: SiteID)
class River(val source: SiteID, val target: SiteID)
class Map(val sites: Array<Site>, val rivers: Array<River>, val mines: Array<Int>)

class Game(val punter: PunterID, val punters: Int, val map: Map) {
    fun move(moves: Array<Move>) = Move.Pass(punter)
}
