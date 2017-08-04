@file:JvmName("Main")

package org.icfp2017

import com.google.gson.Gson
import com.sun.org.apache.xpath.internal.Arg
import org.icfp2017.server.OnlineServer
import org.icfp2017.solver.*

object Arguments {
    var name: String = "Lambada Punter"
    var server: String = "punter.inf.ed.ac.uk"
    var port: Int = 9024
    var strategy: Strategy = RandomFree
    var log: String = "./log.txt"
}

fun main(args: Array<String>) {
    args.forEach {
        val (name, value) = it.split("=")
        when (name) {
            "--name" -> Arguments.name = value
            "--server" -> Arguments.server = value
            "--port" -> Arguments.port = value.toInt()
            "--strategy" -> Arguments.strategy = Strategy.forName(value)
        }
    }

    Logger.log(Gson().toJson(mapOf("arguments" to mapOf(
            "name" to Arguments.name,
            "server" to Arguments.server,
            "port" to Arguments.port,
            "strategy" to Arguments.strategy.javaClass.canonicalName,
            "log" to Arguments.log))))

    val server = OnlineServer()
    Solver.play(server)
}