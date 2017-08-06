package org.icfp2017

import com.google.gson.Gson
import java.io.FileWriter

object Logger {
    var file: FileWriter? = null

    fun logPrint(command: String) = println(command)
    fun logError(command: String) = System.err.println(command)
    fun logFile(command: String) {
        file = file ?: FileWriter(Arguments.log)
        file?.write(command)
        file?.append('\n')
        file?.flush()
    }

    fun log(command: String) = when (Arguments.logging) {
        "error" -> logError(command)
        "print" -> logPrint(command)
        "file" -> logFile(command)
        else -> {
        }
    }

    inline fun <T> measure(action: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val time = System.currentTimeMillis() - start

        log(Gson().toJson(mapOf("measure" to action, "duration" to time)))

        return result
    }

    var _measures = mutableMapOf<String, Pair<Long, List<Long>>>()

    inline fun <T> measurePart(action: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val time = System.currentTimeMillis() - start

        var part = _measures.getOrDefault(action, Pair(0L, emptyList()))
        part = Pair(part.first + time, part.second + time)
        _measures[action] = part
        return  result
    }

    fun measureDone(action: String) {
        log(Gson().toJson(mapOf(
                "measure" to action,
                "duration" to _measures[action]?.first,
                "number" to _measures[action]?.second?.size)))
        _measures.remove(action)
    }
}