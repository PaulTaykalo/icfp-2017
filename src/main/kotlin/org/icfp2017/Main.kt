@file:JvmName("Main")

package org.icfp2017

import com.google.gson.Gson
import com.sun.org.apache.xpath.internal.Arg
import org.icfp2017.server.OfflineServer
import org.icfp2017.server.OnlineServer
import org.icfp2017.solver.*

object Arguments {
    var name: String = "Lambada Punter"
    var server: String = "punter.inf.ed.ac.uk"
    var port: Int = 9024
    var strategy: Strategy = AllYourBaseAreBelongToUsRandom
    var log: String = "./log-${System.currentTimeMillis()}.txt"
    var offline = true
}

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
        }
    }

    Logger.log(Gson().toJson(mapOf("arguments" to mapOf(
            "name" to Arguments.name,
            "server" to Arguments.server,
            "port" to Arguments.port,
            "strategy" to Arguments.strategy.javaClass.canonicalName,
            "log" to Arguments.log,
            "offline" to Arguments.offline))))

    val server = if (Arguments.offline) OfflineServer() else OnlineServer()
    val name = Arguments.name + "[${Arguments.strategy.javaClass.canonicalName}]"
    Solver.play(server, name = name)
}