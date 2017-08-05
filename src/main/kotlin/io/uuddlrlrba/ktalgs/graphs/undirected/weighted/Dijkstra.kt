/*
 * Copyright (c) 2017 Kotlin Algorithm Club
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.uuddlrlrba.ktalgs.graphs.undirected.weighted

import io.uuddlrlrba.ktalgs.datastructures.IndexedPriorityQueue
import io.uuddlrlrba.ktalgs.datastructures.Stack
import io.uuddlrlrba.ktalgs.graphs.NoSuchPathException
import org.icfp2017.Logger

class Dijkstra(graph: UWGraph, val from: Int) {
    /**
     * distTo[v] = distance  of shortest s->v path
     */
    private val distTo: DoubleArray = DoubleArray(graph.V, { if (it == from) 0.0 else Double.POSITIVE_INFINITY })

    /**
     * edgeTo[v] = last edge on shortest s->v path
     */
    private val edgeTo: Array<UWGraph.Edge?> = arrayOfNulls(graph.V)

    /**
     * priority queue of vertices
     */
    private val pq: IndexedPriorityQueue<Double> = IndexedPriorityQueue(graph.V)

    init {
        if (graph.edges().filter { it.weight < 0 }.isNotEmpty()) {
            throw IllegalArgumentException("there is a negative weight edge")
        }

        // relax vertices in order of distance from s
        pq.insert(from, distTo[from])

        while (pq.isNotEmpty()) {
//            if(pq.isNotEmpty()) {
           // Logger.log("\n start of iteration priority queue size is " + pq.size)
//            }
            val v = pq.poll().first

            for (e in graph.adjacentEdges(v)) {
              //  Logger.log("adj edges "+ v + " are " +e)
                relax(e)
            }

        }

    }

    // relax edge e and update pq if changed
    private fun relax(e: UWGraph.Edge) {
        val v = e.v
        val w = e.w
        val distW = distTo[w]
        val distV = distTo[v]
        val weight = e.weight
        //Logger.log("v is $v and w is $w")
        val shouldBeRelaxed = distTo[w] > distTo[v] + e.weight;
        //Logger.log("distTo[w] $distW > distTo[v] $distV + e.weight $weight is $shouldBeRelaxed")
        if (distTo[w] > distTo[v] + e.weight) {
            val newDistW = distTo[v] + e.weight
           // Logger.log("new dist W =  $newDistW")
            distTo[w] = distTo[v] + e.weight
            edgeTo[w] = e

            if (pq.contains(w)) {
             //   Logger.log("pq contains w $w")
                pq.decreaseKey(w, distTo[w])
            } else {
               // Logger.log("pq doest not contain w $w")
                pq.insert(w, distTo[w])

            }
            //Logger.log("size of pq is " + pq.size)
        }

        if (distTo[v] > distTo[w] + e.weight) {
            val newDistV = distTo[w] + e.weight

            //Logger.log("new dist v =  $newDistV")
            distTo[v] = distTo[w] + e.weight
            edgeTo[v] = e

            if (pq.contains(v)) {
              //  Logger.log("pq contains v $v")
                pq.decreaseKey(v, distTo[v])
            } else {
                //Logger.log("pq doest not contain v $v")
                pq.insert(v, distTo[v])

            }
            //Logger.log("size of pq is " + pq.size)
        }
    }

    /**
     * Returns the length of a shortest path from the source vertex `s` to vertex `v`.
     * @param  v the destination vertex
     * @return the length of a shortest path from the source vertex `s` to vertex `v`;
     *         `Double.POSITIVE_INFINITY` if no such path
     */
    fun distTo(v: Int): Double {
        return distTo[v]
    }

    /**
     * Returns true if there is a path from the source vertex `s` to vertex `v`.
     * @param  v the destination vertex
     * @return `true` if there is a path from the source vertex
     *         `s` to vertex `v`; `false` otherwise
     */
    fun hasPathTo(v: Int): Boolean {
        return distTo[v] < java.lang.Double.POSITIVE_INFINITY
    }

    /**
     * Returns a shortest path from the source vertex `s` to vertex `v`.
     * @param  v the destination vertex
     * @return a shortest path from the source vertex `s` to vertex `v`
     *         as an iterable of edges, and `null` if no such path
     */
    fun pathTo(v: Int): Iterable<UWGraph.Edge> {
        if (!hasPathTo(v)) throw NoSuchPathException("There is no path from [$from] to [$v]")
        val path = Stack<UWGraph.Edge>()
        var e = edgeTo[v]
        while (e != null) {
            path.push(e)
            e = edgeTo[e.v]
        }
        return path
    }
}
