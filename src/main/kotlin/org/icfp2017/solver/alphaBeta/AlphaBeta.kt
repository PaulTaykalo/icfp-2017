package org.icfp2017.solver.alphaBeta
import org.icfp2017.*
import org.icfp2017.solver.Strategy
import org.icfp2017.solver.StrategyStateWithGame

data class MinMaxNode(
        val game: Game,
        val move: Move= game.pass(),
        val isMin:Boolean=false,
        val score:Int = Int.MIN_VALUE,
        val alpha:Int = Int.MIN_VALUE,
        val beta:Int = Int.MAX_VALUE,
        val children:List<MinMaxNode> = listOf(),
        val leaf : Boolean  = children.isEmpty()
)

class MinMax(
        val levels: Int = 2,
        val heuristic: (Game)-> Int): Strategy<Game> {

    override fun prepare(game: Game) = game

    fun worstScore(isMin:Boolean):Int{
        if(isMin) return Int.MAX_VALUE
        return Int.MIN_VALUE
    }

    override fun serverMove(moves: Array<Move>, state: Game): Pair<Move, Game> {
        val newGame = applyMoves(moves, state)
        // deep = 2 "scores":[{"punter":0,"score":24295},{"punter":1,"score":88628}}}
        // deep = 3 "scores":[{"punter":0,"score":711},  {"punter":1,"score":82262}

        val move = getBestMove(newGame,levels).move
        return move to newGame
    }

    fun expandNode(game:Game, isMin:Boolean) : List<MinMaxNode>{


        // Selects less developed graph

        val (mine, sites) = game.sitesReachedForMine.maxBy { 0 - it.value.size } ?: return listOf()
        val nicePoints = sites + mine

        val niceRiverClaims = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }
                .map { Claim(game.punter, it.source, it.target) }

        val newGames = niceRiverClaims
                .map { applyMoves(arrayOf(it), game) }
                .zip(niceRiverClaims)
        val nextNodeScore = worstScore(isMin)
        return  newGames.map { MinMaxNode(it.first, it.second, isMin, nextNodeScore) }
    }

    fun buildTree(parentNode: MinMaxNode, levels:Int, alpha: Int, beta: Int) : MinMaxNode {

        val isMin = levels % parentNode.game.punters != 0
        // for non leaf nodes we do recursion
        if(levels == 0) {
            // for leaf nodes we return result
            val leafScore = heuristic(parentNode.game)
            if (isMin) {
                return parentNode.copy(score = leafScore, beta = leafScore)
            } else {
                return parentNode.copy(score = leafScore, alpha = leafScore)
            }
        }
        val nodes = expandNode(parentNode.game, isMin)

        if (nodes.isEmpty())
            return parentNode


        var currentAlpha = alpha
        var currentBeta = beta

        var minimalChildScore = Int.MAX_VALUE
        var maximalChildScore = Int.MIN_VALUE

        var children: List<MinMaxNode> = listOf()


        for (node in nodes) {
            if (currentAlpha >= currentBeta)
                break

            val child = buildTree(node, levels - 1, currentAlpha, currentBeta)

            children += child
            if (isMin) {
                if (child.score < minimalChildScore) {
                    minimalChildScore = child.score
                    currentBeta = minOf(currentBeta, minimalChildScore)
                }
            } else {
                if (child.score > maximalChildScore) {
                    maximalChildScore = child.score
                    currentAlpha = maxOf(currentAlpha, maximalChildScore)
                }
            }

        }

        if(isMin)
        {
            return parentNode.copy(score=minimalChildScore, alpha = alpha, beta = currentBeta,  children = children)
        }else
        {
            return parentNode.copy(score=maximalChildScore, alpha = currentAlpha, beta = beta, children = children)
        }


    }

    fun getBestMove(game:Game, levels:Int) : MinMaxNode {
        val initialNode = MinMaxNode(game)
        val tree = buildTree(initialNode, levels, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
        if (tree.children.isEmpty()) {
            return initialNode.copy(move = game.pass())
        } else {
            return tree.children.maxBy { it -> it.score }!!
        }

    }
}

val MinMaxScore = MinMax { it.currentScore.toInt() }

val AlphaBeta = MinMax { game ->
    val (mine, sites) = game.sitesReachedForMine.maxBy { 0 - it.value.size } ?: return@MinMax 0
    val nicePoints = sites + mine

    val niceRivers = nicePoints
            .flatMap { game.riversForSite[it]!! }
            .filter { it in game.unownedRivers }

    niceRivers.size
}