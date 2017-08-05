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

    fun log(command: String) = logFile(command)

    inline fun <T> measure(action: String, block: () -> T): T {
        val result = measurePart(action, block)
        measureDone(action)
        return result
    }

    var measures = mutableMapOf<String, Pair<Long, Set<Long>>>()

    inline fun <T> measurePart(action: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val time = System.currentTimeMillis() - start

        var part = measures.getOrDefault(action, Pair(0L, setOf<Long>()))
        part = Pair(part.first + time, part.second + time)
        measures[action] = part
        return  result
    }

    fun measureDone(action: String) {
        log(Gson().toJson(mapOf("measure" to action, "duration" to measures[action])))
        measures.remove(action)
    }
}