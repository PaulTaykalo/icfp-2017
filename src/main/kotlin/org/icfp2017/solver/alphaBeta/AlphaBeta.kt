package org.icfp2017.solver.alphaBeta
import org.icfp2017.*
import org.icfp2017.solver.Strategy
import org.icfp2017.solver.StrategyStateWithGame

data class MinMaxNode(
        val game: Game,
        val move: Move= game.pass(),
        val isMin:Boolean=false,
        val score:Double = Double.NEGATIVE_INFINITY,
        val children:List<MinMaxNode> = listOf(),
        val leaf : Boolean  = children.isEmpty()
)

fun worstScore(isMin:Boolean):Double{
    if(isMin) return Double.POSITIVE_INFINITY
    return Double.NEGATIVE_INFINITY
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

fun heuristic(game:Game):Double{
    val (mine, sites) = game.sitesReachedForMine.maxBy { 0 - it.value.size } ?: return 0.0
    val nicePoints = sites + mine

    val niceRivers = nicePoints
            .flatMap { game.riversForSite[it]!! }
            .filter { it in game.unownedRivers }

    return niceRivers.size.toDouble()
}

fun buildTree(parentNode: MinMaxNode, currentLevel:Int, maxLevel:Int) : MinMaxNode {

    val isMin = currentLevel %2 == 0
    // for non leaf nodes we do recursion
    if(currentLevel != maxLevel) {

        val nodes = expandNode(parentNode.game, isMin)
        if(nodes.isEmpty())
            return parentNode
        val children = nodes.map { buildTree(it, currentLevel + 1, maxLevel) }
        if (isMin)
        {
            val minimalChild = children.minBy { it-> it.score}

            val minScore = minimalChild!!.score

            return MinMaxNode(parentNode.game, parentNode.move, isMin, minScore, children)
        }
        else{

            val maxChild = children.minBy { it-> it.score}

            val maxScore = maxChild!!.score
            return MinMaxNode(parentNode.game, parentNode.move, isMin, maxScore, children)
        }
    }

    // for leaf nodes we return result

    return MinMaxNode(parentNode.game, parentNode.move, parentNode.isMin, heuristic(parentNode.game))
}

fun getBestMove(game:Game, howDeep:Int) : MinMaxNode {
    val initialNode = MinMaxNode(game)
    val tree = buildTree(initialNode, 0, howDeep)
    if(tree.children.isEmpty()){
        return initialNode.copy(move = game.pass())
    }else {
        return tree.children.maxBy { it -> it.score }!!
    }

}

object AlphaBeta: Strategy<StrategyStateWithGame> {

    override fun prepare(game: Game): StrategyStateWithGame {
        return StrategyStateWithGame(game)
    }

    override fun serverMove(moves: Array<Move>, state: StrategyStateWithGame): Pair<Move, StrategyStateWithGame> {
        val newGame = applyMoves(moves, state.game)
        val move = getBestMove(newGame,2).move
        return Pair(move, StrategyStateWithGame(newGame))
    }
}
