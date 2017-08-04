package org.icfp2017.graph


import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.BoruvkaMST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.MST
import io.uuddlrlrba.ktalgs.graphs.undirected.weighted.UWGraph
import org.icfp2017.Map


    fun toGraph(map:Map): UWGraph {
        val graph = UWGraph(map.rivers.size);
        for (d in map.rivers)
            graph.addEdge(d.source,d.target, 1.0)
        return graph;
    }

    fun mst(graph:UWGraph): MST {
        val mst =BoruvkaMST(graph)
        return mst
    }

    fun maxId(graph:UWGraph, mst:MST) : UWGraph.Edge{

        val mostAjusted = mst.edges().sortedWith(compareBy({graph.adjacentVertices(it.v).size},{graph.adjacentVertices(it.w).size}))
        return mostAjusted.first()
    }

    fun findMostAdjacentEdgeInSpanningTree(map:Map) : UWGraph.Edge {
        val graph = toGraph(map)
        val mst = mst(graph)
        return maxId(graph, mst)
    }
