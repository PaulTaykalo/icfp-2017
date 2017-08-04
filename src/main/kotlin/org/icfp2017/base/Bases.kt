@file:Suppress("ArrayInDataClass")

package org.icfp2017.base

import com.google.gson.annotations.SerializedName
import org.icfp2017.Move

data class StopCommand(
    @SerializedName("moves") val moves: Array<Move>,
    @SerializedName("scores") val scores: Array<Score>
)
data class Score(val punter: Int, val score: Int)

