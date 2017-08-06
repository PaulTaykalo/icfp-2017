package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import org.icfp2017.*
import org.icfp2017.base.StopCommand
import java.io.BufferedReader
import java.io.InputStreamReader

class OfflineServer {

    var exitAfterMove: Boolean = true
    var serverBehaviour = ServerBehaviour({ json -> send(json) }, { readString() })

    private var inputStream = BufferedReader(InputStreamReader(System.`in`))
    private var outputStream = System.`out`
    val gson = Gson()

    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        serverBehaviour.me(me, callback)
    }

    inline fun <reified State> setup(
            onSetup: (Game) -> Unit,
            onMove: (Array<Move>, State) -> Pair<Move, State>,
            onInterruption: (String) -> Unit,
            onEnd: (StopCommand) -> Unit)
    {
        Logger.log("On offline set up!")
        // Read potential command
        var timeoutsLeft = 10
        while (true) {
            val json = serverBehaviour.readString()
            val response: LinkedTreeMap<String, Any> = Logger.measure("server: parsing general resposnse") {
                val generalType = object : TypeToken<LinkedTreeMap<String, Any>>() {}.type
                gson.fromJson<LinkedTreeMap<String, Any>>(json, generalType)
            }
            Logger.log("response: $response")


            val moves = response.get("move")
            val punter = response.get("punter")
            val punters = response.get("punters")
            val map = response.get("map")

            // is setup reponse
            if (punter != null && punters != null && map != null) {
                Logger.log("it is setup!")
                Logger.measure("server: perform setup") {
                    val setupResponse: SetupResponse = gson.fromJson(json, SetupResponse::class.java)
                    val game = Game(setupResponse.punter, setupResponse.punters, setupResponse.map, setupResponse.settings)
                    onSetup(game)
                }
                continue
            }

            if (moves != null) {
                Logger.log("it is move!")
                val movesResponse: MovesResponse = Logger.measure("server: parsing state from json") {
                    gson.fromJson(json, MovesResponse::class.java)
                }

                val typedMoves: Array<Move> = movesResponse.move.moves.map {
                    it.claim ?: it.pass ?: it.splurge ?: Pass(-1)
                }.toTypedArray()

                val ss = Logger.measure("server: parsing state from json") {
                    gson.fromJson(movesResponse.state, State::class.java)
                }

                val (move, s) = Logger.measure("server: perform move") { onMove(typedMoves, ss) }
                val rr = Logger.measure("server: state serialization") { gson.toJson(s) }

                val moveResponse = MoveRequest(
                        claim = move as? Claim,
                        pass = move as? Pass,
                        splurge = move as? Splurge,
                        state = rr)

                val json = Logger.measure("server: move response serialization") {
                    gson.toJson(moveResponse)
                }
                serverBehaviour.send(json)

                //
                if (exitAfterMove) {
                    break
                }

                continue
            }

            val stop = response.get("stop")
            if (stop != null) {
                Logger.log("it is a stop!")
                val stopResponse: StopGeneralResponse = Logger.measure("server: parsing state from json") {
                    gson.fromJson(json, StopGeneralResponse::class.java)
                }
                val typedMoves: Array<Move> = stopResponse.stop.moves.map {
                    it.claim ?: it.pass ?: it.splurge ?: Pass(-1)
                }.toTypedArray()

                onEnd(StopCommand(typedMoves, stopResponse.stop.scores))
                break
            }

            val timeout = response.get("timeout")
            if (timeout != null) {
                Logger.log("is is a timeout!")
                timeoutsLeft--
                onInterruption("ALARMA!! \$timeoutsLeft")
                continue
            }

            Logger.log("Aaaaaaaaa!")
        }
    }

    inline fun <reified State> ready(punterID: PunterID, futures: Array<FutureRequest>? = null, state: State) {
        Logger.log("On offline ready")
        val json = Logger.measure("server: ready json serialization") {
            val ss = gson.toJson(state)
            gson.toJson(ReadyRequest(punterID, futures, ss))
        }

        serverBehaviour.send(json)
    }

    private fun send(json: JSONString) {
        val message = "${json.length}:"+json
        Logger.log("--> ${message}")
        outputStream.print(message)
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