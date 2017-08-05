@file:JvmName("Main")

package org.icfp2017

import com.google.gson.Gson
import org.icfp2017.server.OfflineServer
import org.icfp2017.server.OnlineServer
import org.icfp2017.solver.*

object Arguments {
    var name: String = "Lambada Punter"
    var server: String = "punter.inf.ed.ac.uk"
    var port: Int = 9028
    var strategy: Strategy<StrategyStateWithGame> = DumbAndGreedy
    var log: String = "./log-${System.currentTimeMillis()}.txt"
    var offline = true
}

val Arguments.strategyName: String get() = strategy.javaClass.simpleName
val Arguments.nameWithStrategy: String get() = "$name [$strategyName]"

fun main(args: Array<String>) {
    if (args.contains("--help")) {
        println("Usage: ./bin/icfp2017 --name=value")
        println("See Main.kt for list of supported arguments")
        return
    }

    args.forEach {
        val (name, value) = it.split("=")
        when (name) {
            "--name" -> Arguments.name = value
            "--server" -> {
                Arguments.server = value
                Arguments.offline = false
            }
            "--port" -> {
                Arguments.port = value.toInt()
                Arguments.offline = false
            }
            "--strategy" -> Arguments.strategy = Strategy.forName(value)
            "--offline" -> Arguments.offline = value.toBoolean()
        }
    }

    Logger.log(Gson().toJson(mapOf("arguments" to mapOf(
            "name" to Arguments.name,
            "server" to Arguments.server,
            "port" to Arguments.port,
            "strategy" to Arguments.strategyName,
            "log" to Arguments.log,
            "offline" to Arguments.offline))))

    if (Arguments.offline) {
        val server = OfflineServer()
        OfflineSolver.play(server)
    } else {
        val server = OnlineServer()
        Solver.play(server)
    }
}