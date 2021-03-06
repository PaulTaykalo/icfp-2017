package org.icfp2017.solver

import com.google.gson.Gson
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.icfp2017.Game
import org.icfp2017.MapModel
import org.icfp2017.River
import org.icfp2017.SiteModel
import org.junit.Test

class StrategyStateWithGameTest {
    @Test
    fun serialization() {
        val strategyStateWithGame = StrategyStateWithGame(
            Game(1, 2,
                MapModel(
                    arrayOf(SiteModel(1), SiteModel(2)),
                    arrayOf(River(1, 2)),
                    arrayOf(1)),
                null
            )
        )
        Gson().toJson(strategyStateWithGame) `should not equal` ""
    }

    @Test
    fun serailizeDeserialize() {
        val strategyStateWithGame = StrategyStateWithGame(
            Game(1, 2,
                MapModel(
                    arrayOf(SiteModel(1),SiteModel(2)),
                    arrayOf(River(1, 2)),
                    arrayOf(1)),
                   null
            )
        )
        val serialized = Gson().toJson(strategyStateWithGame)
        val deserialized = Gson().fromJson(serialized, StrategyStateWithGame::class.java)
        deserialized.game.punter `should equal` strategyStateWithGame.game.punter
        deserialized.game.punters `should equal` strategyStateWithGame.game.punters
    }
}