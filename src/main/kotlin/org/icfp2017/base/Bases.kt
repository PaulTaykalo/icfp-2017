package org.icfp2017.base

import org.icfp2017.Move


class Map {}

class Site {}

class Mine {}

class River {}

class Punter {}



data class StopCommand(val moves: Array<Move>, val scores: Array<Score>)
data class Score(val punter: Int, val score: Int)

