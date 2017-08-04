package org.icfp2017.graph


import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.BoruvkaMST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.MST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph
import org.icfp2017.Logger
import org.icfp2017.Map
import org.icfp2017.River


fun toGraph(map:Map): UWGraph {

        //Logger.log("transforming to graph")
        //Logger.log("map is " + map)
        //Logger.log("sites  " + map.sites)
        //Logger.log("size  " + map.sites.size)


        val vertexNumber =map.sites.size
        val graph = UWGraph(vertexNumber);

        var graphVertexId = 0
        for (site in map.sites){
            graph.VertexToSite.put(graphVertexId, site.id)
            graph.SiteToVertex.put(site.id, graphVertexId)
            graphVertexId++
        }

        for (river in map.rivers){
            val source =  graph.SiteToVertex[river.source]
            val target = graph.SiteToVertex[river.target]
            if(source != null && target != null) {
                graph.addEdge(source, target, 1.0)
            }else
            {
                Logger.log("mapping is screwed up wiht $source and $target")
            }
        }

        return graph;
    }

    private  fun mst(graph:UWGraph): MST {
        val mst =BoruvkaMST(graph)
        return mst
    }

    private fun maxId(graph:UWGraph, mst:MST) : UWGraph.Edge{

        val mostAjusted = mst.edges().sortedWith(compareBy({graph.adjacentVertices(it.v).size},{graph.adjacentVertices(it.w).size}))
        //Logger.log("mst edges  " + mostAjusted.count())
        return mostAjusted.first()
    }

    fun findMostAdjacentEdgeInSpanningTree(graph:UWGraph, mst:MST) : River {
//        Logger.log("graph edges  " + graph.edges().size )
//        Logger.log("graph edges  vals " + graph.edges())
//        Logger.log("graph vertices  " + graph.vertices().count())
//        Logger.log("graph vertices  vals" + graph.vertices())
//        Logger.log("VertexToSite  " + graph.VertexToSite)
//        Logger.log("SiteToVertex  " + graph.SiteToVertex)
        val maxGraphId = maxId(graph, mst)
        val riverSource = graph.VertexToSite[maxGraphId.w]
        val riverTarget = graph.VertexToSite[maxGraphId.v]
        if(riverSource != null && riverTarget != null){
            return River(riverSource, riverTarget,null)
        }
        throw Throwable("the thing thst should not happen");
    }
