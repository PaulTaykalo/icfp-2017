package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.icfp2017.*
import org.icfp2017.base.StopCommand
import java.io.BufferedReader
import java.io.InputStreamReader

class OfflineServer<State> {

    val serverBehaviour = ServerBehaviour({ json -> send(json) }, {
        readString()
    })

    val inputStream = BufferedReader(InputStreamReader(System.`in`))

    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        serverBehaviour.me(me, callback)
    }

    fun setup(onSetup: (Game) -> Unit, onMove: (Array<Move>, State) -> ServerMove<State>, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
        Logger.log("On offline set up!")
        // Read potential command
        var timeoutsLeft = 10
        while (true) {
            val turnsType = object : TypeToken<GeneralResponse<State>>() {}.type
            val response = Gson().fromJson<GeneralResponse<State>>(readString(), turnsType)

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
                val state = response.state!!
                val move = onMove(typedMoves, state)

                val moveResponse = MoveRequest(
                        claim = move.move as? Claim,
                        pass = move.move as? Pass,
                        state = move.state)
                send(Gson().toJson(moveResponse))
                continue
            }

            val stop = response.stop
            if (stop != null) {
                Logger.log("is is a stop!")
                val typedMoves: Array<Move> = stop.moves.map {
                    it.claim ?: it.pass ?: Pass(-1)
                }.toTypedArray()

                onEnd(StopCommand(typedMoves, stop.scores))
                break
            }

            val timeout = response.timeout
            if (timeout != null) {
                Logger.log("is is a timeout!")
                timeoutsLeft--
                onInterruption("ALARMA!! \$timeoutsLeft")
                continue
            }

            Logger.log("Aaaaaaaaa!")
        }
    }

    fun ready(punterID: PunterID, state: State) {
        Logger.log("On offline ready")
        send(Gson().toJson(ReadyRequest(punterID, Gson().toJson(state))))
    }

    fun send(json: JSONString) {
        val message = "${json.length}:"+json
        Logger.log("--> ${message}")
        System.`out`.print(message)
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

