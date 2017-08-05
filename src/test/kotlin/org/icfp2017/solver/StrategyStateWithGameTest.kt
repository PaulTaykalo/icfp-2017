package org.icfp2017.solver

import com.google.gson.Gson
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.icfp2017.Game
import org.icfp2017.MapModel
import org.icfp2017.River
import org.icfp2017.Site
import org.junit.Assert.*
import org.junit.Test

class StrategyStateWithGameTest {
    @Test
    fun serialization() {
        val strategyStateWithGame = StrategyStateWithGame(
            Game(1, 2,
                MapModel(
                    arrayOf(Site(1),Site(2)),
                    arrayOf(River(1, 2, owner = null)),
                    arrayOf(1))
            )
        )
        Gson().toJson(strategyStateWithGame) `should not equal` ""
    }

    @Test
    fun serailizeDeserialize() {
        val strategyStateWithGame = StrategyStateWithGame(
            Game(1, 2,
                MapModel(
                    arrayOf(Site(1),Site(2)),
                    arrayOf(River(1, 2, owner = null)),
                    arrayOf(1))
            )
        )
        val serialized = Gson().toJson(strategyStateWithGame)
        val deserialized = Gson().fromJson(serialized, StrategyStateWithGame::class.java)
        deserialized.game.punter `should equal` strategyStateWithGame.game.punter
        deserialized.game.punters `should equal` strategyStateWithGame.game.punters
    }
}