package org.icfp2017.server

import com.google.gson.Gson
import org.icfp2017.*
import org.icfp2017.base.StopCommand
import java.io.BufferedReader
import java.io.InputStreamReader

class OfflineServer {

    val serverBehaviour = ServerBehaviour({ json -> send(json) }, {
        readString()
    })

    val inputStream = BufferedReader(InputStreamReader(System.`in`))

    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        serverBehaviour.me(me, callback)
    }

    fun setup(onSetup: (Game) -> Unit, onMove: (Array<Move>, State?) -> ServerMove, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
        // Read potential command
        var timeoutsLeft = 10
        while (true) {
            val response: GeneralResponse = Gson().fromJson(readString(), GeneralResponse::class.java)

            if (response.punter != null && response.punters != null && response.map != null) {
                val game = Game(response.punter, response.punters, response.map)
                onSetup(game)
                continue
            }

            val moves = response.moves
            if (moves != null) {
                val typedMoves: Array<Move> = moves.move.map {
                    it.claim ?: it.pass ?: Pass(-1)
                }.toTypedArray()
                val state = moves.state
                val move = onMove(typedMoves, state)

                val moveResponse = MoveResponse(
                        claim = move.move as? Claim,
                        pass = move.move as? Pass,
                        state = move.state)
                send(Gson().toJson(moveResponse))
                continue
            }

            val stop = response.stop
            if (stop != null) {
                val typedMoves: Array<Move> = stop.moves.map {
                    it.claim ?: it.pass ?: Pass(-1)
                }.toTypedArray()

                onEnd(StopCommand(typedMoves, stop.scores))
                break
            }

            val timeout = response.timeout
            if (timeout != null) {
                timeoutsLeft--
                onInterruption("ALARMA!! \$timeoutsLeft")
                continue
            }

            // Waat?
        }
    }

    fun ready(punterID: PunterID, state: State?) {
        send(Gson().toJson(ReadyRequest(punterID, state)))
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