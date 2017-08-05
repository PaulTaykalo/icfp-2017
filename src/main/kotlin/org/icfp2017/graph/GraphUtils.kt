package org.icfp2017.graph


import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.BoruvkaMST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.MST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph.Edge
import org.icfp2017.Game
import org.icfp2017.Logger
import org.icfp2017.Map
import org.icfp2017.River

class GraphUtils(game: Game) {

    var graph: UWGraph? = null;
    var mst: MST? = null;
    var mstEdges: Iterable<Edge>? = null;

    init {
        graph = toGraph(game.map)
        mst = BoruvkaMST(graph!!)
        mstEdges = mst!!.edges()
    }

    fun mostConnectedRivers(rivers: List<River>): List<River> {
        val edge = findMostAdjacentEdgeInSpanningTree(graph!!, mst!!)
        return rivers.filter { (it.source == edge.source && it.target == edge.target) || ((it.source == edge.target && it.target == edge.source)) }
    }


    fun riversCloseToBases(rivers: List<River>, map: Map): List<River> {
        val baseRivers = rivers.filter { map.mines.contains(it.target) || map.mines.contains(it.source) }
        val priorityBaseRivers = baseRivers.sortedWith(compareBy({ graph!!.adjacentEdges(it.target).size }, { graph!!.adjacentEdges(it.source).size }))
        return priorityBaseRivers
    }

    fun toGraph(map: Map): UWGraph {

        //Logger.log("transforming to graph")
        //Logger.log("map is " + map)
        //Logger.log("sites  " + map.sites)
        //Logger.log("size  " + map.sites.size)


        val vertexNumber = map.sites.size
        val graph = UWGraph(vertexNumber);

        var graphVertexId = 0
        for (site in map.sites) {
            graph.VertexToSite.put(graphVertexId, site.id)
            graph.SiteToVertex.put(site.id, graphVertexId)
            graphVertexId++
        }

        for (river in map.rivers) {
            val source = graph.SiteToVertex[river.source]
            val target = graph.SiteToVertex[river.target]
            if (source != null && target != null) {
                graph.addEdge(source, target, 1.0)
            } else {
                Logger.log("mapping is screwed up wiht $source and $target")
            }
        }

        return graph;
    }

    private fun mostFatMstRiver(): Edge {

        val mostAjusted = mstEdges!!.sortedWith(compareBy({ graph!!.adjacentVertices(it.v).size }, { graph!!.adjacentVertices(it.w).size }))
        //Logger.log("mst edges  " + mostAjusted.count())
        return mostAjusted.first()
    }

    fun findMostAdjacentEdgeInSpanningTree(graph: UWGraph, mst: MST): River {
//        Logger.log("graph edges  " + graph.edges().size )
//        Logger.log("graph edges  vals " + graph.edges())
//        Logger.log("graph vertices  " + graph.vertices().count())
//        Logger.log("graph vertices  vals" + graph.vertices())
//        Logger.log("VertexToSite  " + graph.VertexToSite)
//        Logger.log("SiteToVertex  " + graph.SiteToVertex)
        val mostFatRiver = mostFatMstRiver()
        val riverSource = graph.VertexToSite[mostFatRiver.w]
        val riverTarget = graph.VertexToSite[mostFatRiver.v]
        if (riverSource != null && riverTarget != null) {
            return River(riverSource, riverTarget, null)
        }
        throw Throwable("the thing thst should not happen");
    }
}
