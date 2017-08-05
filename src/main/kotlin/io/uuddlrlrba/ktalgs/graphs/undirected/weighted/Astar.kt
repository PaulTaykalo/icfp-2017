package io.uuddlrlrba.ktalgs.graphs.undirected.weighted

import sun.security.provider.certpath.Vertex
import java.util.*


public class AStar {

    /**
     * Find the path using the A* algorithm from start vertex to end vertex or NULL if no path exists.

     * @param graph
     * *          Graph to search.
     * *
     * @param start
     * *          Start vertex.
     * *
     * @param goal
     * *          Goal vertex.
     * *
     * *
     * @return
     * *          List of Edges to get from start to end or NULL if no path exists.
     */
    fun aStar(graph: UWGraph, start: Int, goal: Int): List<UWGraph.Edge>? {
        val size = graph.V // used to size data structures appropriately
        val closedSet = HashSet<Int>(size) // The set of nodes already evaluated.
        val openSet = ArrayList<Int>(size) // The set of tentative nodes to be evaluated, initially containing the start node
        openSet.add(start)
        val cameFrom = HashMap<Int, Int>(size) // The map of navigated nodes.

        val gScore = HashMap<Int, Double>() // Cost from start along best known path.
        gScore.put(start, 0.0)

        // Estimated total cost from start to goal through y.
        val fScore = HashMap<Int, Double>()
        for (v in graph.vertices())
            fScore.put(v, Double.MAX_VALUE)
        fScore.put(start, heuristicCostEstimate(start, goal))

        val comparator = object : Comparator<Int> {
            /**
             * {@inheritDoc}
             */
            override fun compare(o1: Int, o2: Int): Int {
                val score1 = fScore[o1]
                val score2 = fScore[o2]
                if (score1!! < score2!!)
                    return -1
                if (score1!! < score2!!)
                    return 1
                return 0
            }
        }

        while (!openSet.isEmpty()) {
            val current = openSet.get(0)

            if (comparator.compare(current,goal) == 0)
                return reconstructPath(cameFrom, goal)

            openSet.removeAt(0)
            closedSet.add(current)
            for (edge in graph.adjacentEdges(current)) {
                val neighbor = edge.w
                if (closedSet.contains(neighbor))
                    continue // Ignore the neighbor which is already evaluated.

                val currentG = gScore[current]

                val tenativeGScore = currentG!! + distanceBetween(graph, current, neighbor) // length of this path.
                if (!openSet.contains(neighbor))
                    openSet.add(neighbor) // Discover a new node
                else {
                    val neighborG = gScore[neighbor]
                    if (tenativeGScore >= neighborG!!)
                        continue
                }


                // This path is the best until now. Record it!
                cameFrom.put(neighbor, current)
                gScore.put(neighbor, tenativeGScore)
                val neighborG = gScore[neighbor]
                val estimatedFScore = neighborG!! + heuristicCostEstimate(neighbor, goal)
                fScore.put(neighbor, estimatedFScore)

                // fScore has changed, re-sort the list
                Collections.sort(openSet, comparator)
            }
        }

        return null
    }

    /**
     * Default distance is the edge cost. If there is no edge between the start and next then
     * it returns Integer.MAX_VALUE;
     */
    protected fun distanceBetween(graph: UWGraph, start: Int, next: Int): Double {
        for (e in graph.adjacentEdges(start)) {
            if (e.w == next)
                return e.weight
        }
        return Double.MAX_VALUE
    }

    /**
     * Default heuristic: cost to each vertex is 1.
     */
    protected fun heuristicCostEstimate(start: Int, goal: Int): Double {
        return 1.0
    }

    private fun reconstructPath(cameFrom: Map<Int, Int>, currentIndex: Int): List<UWGraph.Edge> {
        var current:Int? = currentIndex
        val totalPath = ArrayList<UWGraph.Edge>()

        while (current != null) {
            val previous = current
            current = cameFrom[current]
            if (current != null) {
                val edge =  UWGraph.Edge(current, previous, 1.0)
                totalPath.add(edge)
            }
        }
        Collections.reverse(totalPath)
        return totalPath
    }
}