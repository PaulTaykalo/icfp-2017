package org.icfp2017.solver

import org.icfp2017.*
import org.icfp2017.graph.GraphUtils

data class AllYourBaseAreBelongToUsExpansionState(
        val punter: PunterID,
        val sitesForSite: SitesForSite,
        val unownedRivers: Set<River>,
        val ownedRivers: Set<River>,
        val myRivers: Set<River>,
        val sitesReachedForMine: Reachability,
        val riversForSite: RiversForSite,
        val mines: Set<SiteID>
)

// captures rivers close to bases first and then does spanning tree
object AllYourBaseAreBelongToUsExpansion : Strategy<AllYourBaseAreBelongToUsExpansionState>{

    override fun prepare(game: Game): AllYourBaseAreBelongToUsExpansionState {
        return AllYourBaseAreBelongToUsExpansionState(
                punter = game.punter,
                sitesForSite = game.sitesForSite,
                unownedRivers = game.unownedRivers,
                ownedRivers = game.ownedRivers,
                myRivers = game.myRivers,
                sitesReachedForMine = game.sitesReachedForMine,
                riversForSite = game.riversForSite,
                mines = game.mines
        )
    }

    override fun serverMove (moves: Array<Move>, state: AllYourBaseAreBelongToUsExpansionState): Pair<Move, AllYourBaseAreBelongToUsExpansionState> {
        val graphUtils = GraphUtils(state.sitesForSite, state.unownedRivers, state.ownedRivers)
        val newState = applyMoves(moves, state)
        graphUtils.updateState(
                newState.sitesForSite,
                newState.unownedRivers,
                newState.ownedRivers,
                newState.myRivers
        )
        val rivers = newState.unownedRivers.toList()
        if (rivers.isEmpty()) return Pair(Pass(newState.punter),  newState)

        val baseRivers =  graphUtils.riversCloseToBases(rivers, newState.mines, newState.riversForSite)
        if(baseRivers.isNotEmpty()){
            val claim = baseRivers.first()
            //Logger.log("base river claim $claim")
            return Pair(Claim(state.punter, claim.source, claim.target), newState)
        }
        // if all base rivers are captures, do most connected things
        val mostConnected = graphUtils.mostConnectedRivers(rivers)
        if(mostConnected.isNotEmpty()) {
            val claim = mostConnected.first()
            //Logger.log("most connected claim $claim")
            return Pair(Claim(state.punter, claim.source, claim.target), newState)
        }


        // if minimal spanning tree is captured, do whatever is left
        val claim = rivers.first()
        return  Pair(Claim(state.punter, claim.source, claim.target), newState)
    }

    private fun applyMoves(moves: Array<Move>, state: AllYourBaseAreBelongToUsExpansionState): AllYourBaseAreBelongToUsExpansionState {

        // Apply moves to map.
        var newUnownedRivers = state.unownedRivers
        var newOwnedRivers = state.ownedRivers
        var newMyRivers = state.myRivers
        var newSiteReachebleForMine = state.sitesReachedForMine

        moves.forEach { move ->
            if (move !is Claim) return@forEach
            val river = River(move.source, move.target)
            newUnownedRivers -= river
            newOwnedRivers += river

            if (move.punter == state.punter) {
                newMyRivers += river
                newSiteReachebleForMine = updateSitesReachability(newSiteReachebleForMine, river,newMyRivers, state.riversForSite)
            }
        }

        return AllYourBaseAreBelongToUsExpansionState(
                punter = state.punter,
                ownedRivers = newOwnedRivers,
                unownedRivers = newUnownedRivers,
                myRivers = newMyRivers,
                sitesReachedForMine = newSiteReachebleForMine,
                sitesForSite = state.sitesForSite,
                mines = state.mines,
                riversForSite = state.riversForSite
        )
    }
}