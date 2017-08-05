package org.icfp2017.server

import com.google.gson.Gson
import org.icfp2017.*
import org.icfp2017.base.StopCommand
import org.icfp2017.solver.StrategyStateWithGame
import sun.rmi.runtime.Log
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

    fun setup(onSetup: (Game) -> Unit, onMove: (Array<Move>, StrategyStateWithGame) -> ServerMove, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
        Logger.log("On offline set up!")
        // Read potential command
        var timeoutsLeft = 10
        while (true) {
            val response: GeneralResponse = Logger.measure("parsing reposnse") {
                Gson().fromJson(readString(), GeneralResponse::class.java)
            }
            Logger.log("responce: ${response}")

            if (response.punter != null && response.punters != null && response.map != null) {
                val game = Game(response.punter, response.punters, response.map)
                onSetup(game)
                continue
            }

            val moves = response.moves
            if (moves != null) {
                Logger.log("is is a move!")
                val typedMoves: Array<Move> = moves.move.map {
                    it.claim ?: it.pass ?: Pass(-1)
                }.toTypedArray()
                Logger.log("try state: ${response.state}")
                val state = response.state!!
                Logger.log("1")
                val move = onMove(typedMoves, state)
                Logger.log("move strat: ${move}")

                val moveResponse = MoveResponse(
                        claim = move.move as? Claim,
                        pass = move.move as? Pass,
                        state = move.state)
                Logger.log("move responce to send: ${moveResponse}")
                send(Logger.measure("serialize response") {
                    Gson().toJson(moveResponse)
                })
                Logger.log("move responce sent!")
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

    fun ready(punterID: PunterID, state: StrategyStateWithGame) {
        Logger.log("On offline ready")
        send(Gson().toJson(ReadyRequest(punterID, state)))
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