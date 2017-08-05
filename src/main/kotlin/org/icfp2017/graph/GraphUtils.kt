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
    var mstEdges: List<Edge>? = null;
    var mostAjustedMst: List<Edge>? = null;

    val riverToEdge:HashMap<River, Edge> = hashMapOf()
    val edgeToRiver:HashMap<Edge, River> = hashMapOf()

    val siteToVertex:HashMap<Int, Int> = hashMapOf()
    val vertexToSite:HashMap<Int, Int> = hashMapOf()

    val freeRivers : MutableSet<River> = mutableSetOf()
    val takenRivers : MutableSet<River> = mutableSetOf()
    val ourRivers : MutableSet<River> = mutableSetOf()
    val theirRivers : MutableSet<River> = mutableSetOf()
    val allRivers : MutableSet<River> = mutableSetOf()

    val freeEdges : MutableSet<Edge> = mutableSetOf()
    val takenEdges : MutableSet<Edge> = mutableSetOf()
    val ourEdges : MutableSet<Edge> = mutableSetOf()
    val theirEdges : MutableSet<Edge> = mutableSetOf()
    val allEdges : MutableSet<Edge> = mutableSetOf()



    val mostAjustedMstFree: MutableSet<Edge> = mutableSetOf();


    init {
       initState(game)
    }

    fun toGraph(game: Game): UWGraph {

        //Logger.log("transforming to graph")
        //Logger.log("map is " + map)
        //Logger.log("sites  " + map.sites)
        //Logger.log("size  " + map.sites.size)

        val map = game.map

        val vertexNumber = map.sites.size
        val graph = UWGraph(vertexNumber);

        var graphVertexId = 0
        for (site in map.sites) {
            vertexToSite.put(graphVertexId, site.id)
            siteToVertex.put(site.id, graphVertexId)
            graphVertexId++
        }

        for (river in map.rivers) {
            val source = siteToVertex[river.source]
            val target = siteToVertex[river.target]
            if (source != null && target != null) {
                var edge = graph.addEdge(source, target, 1.0, river)
                if(river.owner != game.punter) {
                    edge = graph.addEdge(source, target, java.lang.Double.POSITIVE_INFINITY, river)

                }
                riverToEdge.put(river, edge)
                edgeToRiver.put(edge, river)

            } else {
                Logger.log("mapping is screwed up wiht $source and $target")
            }
        }

        return graph;
    }

    fun initState(game:Game){
        graph = toGraph(game)
        mst = BoruvkaMST(graph!!)
        mstEdges = mst!!.edges().toList()
        mostAjustedMst = mstEdges!!.sortedWith(compareBy({ graph!!.adjacentVertices(it.v).size }, { graph!!.adjacentVertices(it.w).size }))
        mostAjustedMstFree.addAll(mst!!.edges());
    }

    fun updateState(game:Game){

        // should not happen, should do delta
        initState(game)

        for (river in game.map.rivers)
        {
            allRivers.add(river)
            allEdges.add(riverToEdge[river] as Edge)
            if(river.owner == null){
                freeRivers.add(river)
                freeEdges.add(riverToEdge[river] as Edge)
            }else
            {
                takenRivers.add(river)
                takenEdges.add(riverToEdge[river] as Edge)

            }

            if(river.owner == game.punter){
                ourRivers.add(river)
                ourEdges.add(riverToEdge[river] as Edge)
            }else
            {
                theirRivers.add(river)
                theirEdges.add(riverToEdge[river] as Edge)
            }

        }
    }
    fun mostConnectedRivers(rivers: Iterable<River>): List<River> {
        val edge = findMostAdjacentEdgeInSpanningTree(graph!!, mst!!)
        return rivers.filter {  (it.source == edge.source && it.target == edge.target) || ((it.source == edge.target && it.target == edge.source)) }
    }


    fun riversCloseToBases(rivers: List<River>, map: Map): List<River> {
        val baseRivers = rivers.filter { map.mines.contains(it.target) || map.mines.contains(it.source) }
        val priorityBaseRivers = baseRivers.sortedWith(compareBy({ graph!!.adjacentEdges(it.target).size }, { graph!!.adjacentEdges(it.source).size }))
        return priorityBaseRivers
    }



    fun findMostAdjacentEdgeInSpanningTree(graph: UWGraph, mst: MST): River {
//        Logger.log("graph edges  " + graph.edges().size )
//        Logger.log("graph edges  vals " + graph.edges())
//        Logger.log("graph vertices  " + graph.vertices().count())
//        Logger.log("graph vertices  vals" + graph.vertices())
//        Logger.log("VertexToSite  " + graph.VertexToSite)
//        Logger.log("SiteToVertex  " + graph.SiteToVertex)
        val mostFatEdge =  mostAjustedMstFree.intersect(freeEdges).first()
        val mostFatRiver = edgeToRiver[mostFatEdge]

        if (mostFatRiver != null) return mostFatRiver

        throw Throwable("the thing thst should not happen");
    }
}
