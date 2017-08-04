package org.icfp2017.base

import org.icfp2017.Move

data class StopCommand(val moves: Array<Move>, val scores: Array<Score>)
data class Score(val punter: Int, val score: Int)

