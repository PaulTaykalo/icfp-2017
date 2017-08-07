package org.icfp2017.solver.alphaBeta
import org.icfp2017.*
import org.icfp2017.graph.GraphUtils
import org.icfp2017.solver.SpanningTree
import org.icfp2017.solver.Strategy
import org.icfp2017.solver.StrategyStateWithGame

data class MinMaxNode(
        val game: Game,
        val move: Move= game.pass(),
        val level:Int = 0,
        val isMin:Boolean=false,
        val score:Int = Int.MIN_VALUE,
        val alpha:Int = Int.MIN_VALUE,
        val beta:Int = Int.MAX_VALUE,
        val children:List<MinMaxNode> = listOf()

)

class MinMax(
        val levels: Int =1,
        val heuristic: (Game, Move)-> Int): Strategy<Game> {

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

    fun expandNode(game:Game, level:Int, isMin:Boolean) : List<MinMaxNode>{


        // Selects less developed graph

        //Logger.log("expansion size : " + game.sitesReachedForMine.size)
        val (mine, sites) = game.sitesReachedForMine.maxBy {  it.value.size } ?: return listOf()
        val nicePoints = sites + mine

        val niceRiverClaims = nicePoints
                .flatMap { game.riversForSite[it]!! }
                .filter { it in game.unownedRivers }
                .map { Claim(game.punter, it.source, it.target) }

        val newGames = niceRiverClaims
                .map { applyMoves(arrayOf(it), game) }
                .zip(niceRiverClaims)
        val nextNodeScore = worstScore(isMin)
        return  newGames.map { MinMaxNode(it.first, it.second, level, isMin, nextNodeScore) }
    }

    fun buildTree(parentNode: MinMaxNode, levels:Int, maxLevels:Int, alpha: Int, beta: Int) : MinMaxNode {

        val maxFlag = Math.abs(maxLevels - parentNode.game.punters) % parentNode.game.punters

         val isMin =levels % parentNode.game.punters == maxFlag
        //val isMin = levels % parentNode.game.punters != 0
        // for non leaf nodes we do recursion
        if(levels == 0) {
            // for leaf nodes we return result
            val leafScore = heuristic(parentNode.game, parentNode.game.pass())
            //Logger.log("level 0 , score = $leafScore")
            return parentNode.copy(score = leafScore)
        }
        val nodes = expandNode(parentNode.game, levels-1, isMin)

        if (nodes.isEmpty()){
            //Logger.log("nohting is found")
            return parentNode

        }



        var currentAlpha = alpha
        var currentBeta = beta
        var currentScore= Int.MIN_VALUE
        if(isMin)
            currentScore=Int.MAX_VALUE

        var children: List<MinMaxNode> = listOf()


        for (node in nodes) {
            val child = buildTree(node, levels - 1, maxLevels, currentAlpha, currentBeta)

            children += child
            if (isMin) {
                if (child.score < currentScore) {
                   currentScore = child.score

                }
                if (child.score < currentBeta) {
                    currentBeta = child.score
                }
            }
            else {

                if (child.score > currentScore) {
                    currentScore = child.score
                }
                if (child.score > currentAlpha) {
                    currentAlpha = child.score
                }

            }
            if (currentAlpha >= currentBeta)
                break

        }

        val childrenCount = children.size
        if(isMin)
        {
           //Logger.log("level: $levels, isMin : $isMin, score: $currentScore, alpha:  $alpha, beta:$beta, childrens: $childrenCount")
            // currentScore = children.minBy { it.score }!!.score
            return parentNode.copy(score=currentScore, alpha = alpha, beta = currentBeta,  children = children)

        }else
        {
            //Logger.log("level: $levels, isMin : $isMin, score: $currentScore, alpha:  $currentAlpha,  beta: $beta, childrens: $childrenCount")
            //currentScore = children.maxBy { it.score }!!.score
            return parentNode.copy(score=currentScore, alpha = currentAlpha, beta = beta, children = children)

        }


    }

    fun getBestMove(game:Game, levels:Int) : MinMaxNode {
        val initialNode = MinMaxNode(game)
        val tree = buildTree(initialNode, levels, maxLevels = levels,alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
        if (tree.children.isEmpty()) {
            return initialNode.copy(move = game.pass())
        } else {
            return tree.children.maxBy { it -> it.score }!!
        }

    }
}

val MinMaxScore = MinMax {game: Game, move: Move->
                        game.currentScore.toInt()}


val AlphaBeta = MinMax { game: Game, move: Move ->
    val (mine, sites) = game.sitesReachedForMine.maxBy { 0 - it.value.size } ?: return@MinMax 0
    val nicePoints = sites + mine

    val niceRivers = nicePoints
            .flatMap { game.riversForSite[it]!! }
            .filter { it in game.unownedRivers }

    niceRivers.size
}

val MinMaxScoreSpanning = MinMax {
    game: Game, move: Move->
        val graphUtils = GraphUtils(game)
        val mostConnected = graphUtils.mostConnectedRivers(game.unownedRivers)

    if(move is Claim) {
        if (mostConnected.find { (it.target == move.target && it.source == move.source) || (it.source == move.target && it.target == move.source) } != null){
            game.currentScore.toInt()*2
        }
    }
    game.currentScore.toInt()

}