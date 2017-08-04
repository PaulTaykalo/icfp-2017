package org.icfp2017.server

import org.icfp2017.*
import org.icfp2017.base.StopCommand
import java.io.BufferedReader
import java.io.InputStreamReader

class OfflineServer : Server {

    val serverBehaviour = ServerBehaviour({ json -> send(json) }, {
        readString()
    })

    val inputStream = BufferedReader(InputStreamReader(System.`in`))

    override fun me(me: PunterName, callback: (PunterName) -> Unit) {
        callback("PewPew")
    }

    override fun setup(callback: (Game) -> Unit) {
        serverBehaviour.setup(callback)
    }

    override fun ready(punterID: PunterID, onMove: (Array<Move>) -> Move, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
        serverBehaviour.ready(punterID, onMove, onInterruption, onEnd)
    }

    fun send(json: JSONString) {
        System.`out`.println("${json.length}:"+json)
    }

    private fun readString(): JSONString {
        val size = readSize()
        val result = readBytes(size)
        Logger.log("<-- $result")
        return result
    }

    private fun readBytes(size: Int): String {
        var left = size
        return buildString {
            while (true) {
                val ch = inputStream.read().toChar()
                append(ch)
                left--
                if (left == 0) {
                    break
                }
            }
        }
    }

    private fun readSize(): Int {
        val string = buildString {
            while (true) {
                val ch = inputStream.read().toChar()
                if (ch == ':') break
                append(ch)
            }
        }
        Logger.log("Is about to read $string")
        return string.toInt()
    }
}