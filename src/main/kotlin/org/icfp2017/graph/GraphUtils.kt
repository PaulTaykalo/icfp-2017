package org.icfp2017.graph


import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.*
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph.Edge
import org.icfp2017.Game
import org.icfp2017.Logger
import org.icfp2017.River
import org.icfp2017.*
import org.icfp2017.MapModel

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

        val vertexNumber = game.sitesForSite.keys.size
        val graph = UWGraph(vertexNumber);

        var graphVertexId = 0
        for (site in game.sitesForSite.keys) {
            vertexToSite.put(graphVertexId, site)
            siteToVertex.put(site, graphVertexId)
            graphVertexId++
        }

        for (river in game.unownedRivers + game.ownedRivers) {
            val source = siteToVertex[river.source]
            val target = siteToVertex[river.target]
            if (source != null && target != null) {
                var edge = graph.addEdge(source, target, 1.0, river)
//                if(river !in game.myRivers) {
//                    edge = graph.addEdge(source, target, java.lang.Double.POSITIVE_INFINITY, river)
//
//                }
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

        for (river in game.unownedRivers + game.ownedRivers)
        {
            allRivers.add(river)
            allEdges.add(riverToEdge[river] as Edge)
            if(river !in game.unownedRivers){
                takenRivers.add(river)
                takenEdges.add(riverToEdge[river] as Edge)
                if(river in game.myRivers){
                    ourRivers.add(river)
                    ourEdges.add(riverToEdge[river] as Edge)
                }else
                {
                    theirRivers.add(river)
                    theirEdges.add(riverToEdge[river] as Edge)
                }
            }else // no one took rivers,they are free
            {
                freeRivers.add(river)
                freeEdges.add(riverToEdge[river] as Edge)

            }



        }
    }
    fun mostConnectedRivers(rivers: Iterable<River>): List<River> {
        val edge = findMostAdjacentEdgeInSpanningTree()
        if(edge == null) {
            return rivers.toList()
        }else
        {
            return rivers.filter { (it.source == edge.source && it.target == edge.target) || ((it.source == edge.target && it.target == edge.source)) }
        }
    }


    fun vertexFromSite(site:Int):Int{
        val res = siteToVertex[site]
        if(res == null)
            throw NullPointerException()
        return res
    }

    fun riversCloseToBases(rivers: List<River>, game: Game): List<River> {
        val baseRivers = game.mines.flatMap { game.riversForSite[it]!! }

        val priorityBaseRivers = baseRivers.sortedWith(compareBy({ graph!!.adjacentEdges(vertexFromSite(it.target)).size }, { graph!!.adjacentEdges(vertexFromSite(it.source)).size }))
        return priorityBaseRivers.intersect(rivers).toList()
    }

    fun findPath(startSiteId:Int, endSiteId:Int) :Iterable<River>{
        val startVertexId = siteToVertex[startSiteId];
        val endVertexId = siteToVertex[endSiteId];
        val astar = AStar()

        val path = astar.aStar(graph!!, startVertexId!!, endVertexId!!)

        if(path!=null){

            //Logger.log("path is found "  + path)

            return path.map { River(vertexToSite[it.v] as SiteID, vertexToSite[it.w] as SiteID) }

        }else{
            return listOf()
        }

    }


    fun findMostAdjacentEdgeInSpanningTree(): River? {
//        Logger.log("graph edges  " + graph.edges().size )
//        Logger.log("graph edges  vals " + graph.edges())
//        Logger.log("graph vertices  " + graph.vertices().count())
//        Logger.log("graph vertices  vals" + graph.vertices())
//        Logger.log("VertexToSite  " + graph.VertexToSite)
//        Logger.log("SiteToVertex  " + graph.SiteToVertex)
        val mostFatEdge =  mostAjustedMstFree.intersect(freeEdges).firstOrNull()
        if(mostFatEdge == null)
            return null;
        val mostFatRiver = edgeToRiver[mostFatEdge]

        if (mostFatRiver != null) return mostFatRiver

        throw Throwable("the thing thst should not happen");
    }
}
