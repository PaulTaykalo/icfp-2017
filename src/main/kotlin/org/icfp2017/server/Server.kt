@file:Suppress("ArrayInDataClass")

package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.icfp2017.*
import org.icfp2017.base.Score
import org.icfp2017.base.StopCommand
import org.icfp2017.solver.StrategyStateWithGame
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


typealias JSONString = String

data class ServerMove<S>(
    @SerializedName("move") val move: Move,
    @SerializedName("state") val state: S?
)

data class ReadyRequest<S>(
    @SerializedName("ready") val ready: PunterID,
    @SerializedName("state") val state: S?
)

data class MoveResponse(
    @SerializedName("claim") val claim: Claim?,
    @SerializedName("pass") val pass: Pass?
)

data class MoveRequest<S>(
    @SerializedName("claim") val claim: Claim?,
    @SerializedName("pass") val pass: Pass?,
    @SerializedName("state") val state: S?
)


data class MovesArrayResponse(
    @SerializedName("moves") val move: Array<MoveResponse>
)

data class TimeoutResponse(val state: Any)

data class StopResponse(
    @SerializedName("moves") val moves: Array<MoveResponse>,
    @SerializedName("scores") val scores: Array<Score>
)

data class GeneralResponse<S>(
    // setup
    @SerializedName("punter") val punter: PunterID?,
    @SerializedName("punters") val punters: Int?,
    @SerializedName("map") val map: MapModel?,
    // move
    @SerializedName("move") val moves: MovesArrayResponse?,
    // stop
    @SerializedName("stop") val stop: StopResponse?,
    // offline
    @SerializedName("timeout") val timeout: TimeoutResponse?,
    @SerializedName("state") val state: S?
)

//class OnlineServer(serverName: String = Arguments.server, serverPort: Int = Arguments.port) {
//
//    private val serverBehaviour = ServerBehaviour({ json -> send(json) }, { readString() })
//    private val client: Socket
//    private val outputStream: OutputStream
//    private val inputStream: InputStream
//
//    init {
//        val inteAddress = InetAddress.getByName(serverName)
//        val socketAddress = InetSocketAddress(inteAddress, serverPort)
//        client = Socket()
//        val timeoutInMs = 10 * 1000
//        client.connect(socketAddress, timeoutInMs)
//        outputStream = client.outputStream
//        inputStream = client.inputStream
//    }
//
//    fun me(me: PunterName, callback: (PunterName) -> Unit) {
//        serverBehaviour.me(me, callback)
//    }
//
//    fun setup(callback: (Game) -> Unit) {
//        val response: GameModel = Gson().fromJson(readString(), GameModel::class.java)
//        val game = Game(response.punter, response.punters, response.map)
//        callback(game)
//    }
//
//    fun ready(punterID: PunterID, state: StrategyStateWithGame?, onMove: (Array<Move>, StrategyStateWithGame?) -> ServerMove, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
//        send(Gson().toJson(ReadyRequest(punterID, state)))
//
//        // Read potential command
//        var timeoutsLeft = 10
//        while (true) {
//            val response: GeneralResponse = Gson().fromJson(readString(), GeneralResponse::class.java)
//            val moves = response.moves
//            if (moves != null) {
//                val typedMoves: Array<Move> = moves.move.map {
//                    it.claim ?: it.pass ?: Pass(-1)
//                }.toTypedArray()
//                val state = response.state
//                val move = onMove(typedMoves, state)
//
//                val moveResponse = MoveResponse(
//                    claim = move.move as? Claim,
//                    pass = move.move as? Pass,
//                    state = move.state)
//                send(Gson().toJson(moveResponse))
//                continue
//            }
//
//            val stop = response.stop
//            if (stop != null) {
//                val typedMoves: Array<Move> = stop.moves.map {
//                    it.claim ?: it.pass ?: Pass(-1)
//                }.toTypedArray()
//
//                onEnd(StopCommand(typedMoves, stop.scores))
//                break
//            }
//
//            val timeout = response.timeout
//            if (timeout != null) {
//                timeoutsLeft--
//                onInterruption("ALARMA!! \$timeoutsLeft")
//                continue
//            }
//
//            // Waat?
//        }
//    }
//
//    fun setup(callback: (Game) -> Unit) {
//        serverBehaviour.setup(callback)
//    }
//
//    fun ready(punterID: PunterID, onMove: (Array<Move>) -> Move, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
//        serverBehaviour.ready(punterID, onMove, onInterruption, onEnd)
//    }
//
//    private fun send(json: String) {
//        Logger.log(json)
//        val byteArray = json.toByteArray()
//        val prefix = "${byteArray.size}:".toByteArray()
//        outputStream.write(prefix)
//        outputStream.write(byteArray)
//        outputStream.flush()
//    }
//
//
//    private fun readString(): String {
//        val size = readSize()
//        val result = readBytes(size)
//        Logger.log(result)
//        return result
//    }
//
//    private fun readBytes(size: Int): String {
//        var left = size
//        return buildString {
//            while (true) {
//                val ch = inputStream.read().toChar()
//                append(ch)
//                left--
//                if (left == 0) {
//                    break
//                }
//            }
//        }
//    }
//
//    private fun readSize(): Int {
//        val string = buildString {
//            while (true) {
//                val ch = inputStream.read().toChar()
//                if (ch == ':') break
//                append(ch)
//            }
//        }
//        return string.toInt()
//    }
//
//}

class ServerBehaviour(val send: (JSONString) -> Unit, val readString: () -> JSONString)  {

    private data class MeRequest(@SerializedName("me") val me: String)
    private data class MeResponse(@SerializedName("you") val you: String)

    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        val me = Gson().toJson(MeRequest(me))
        Logger.log(me)
        send(me)
        val response: MeResponse = Gson().fromJson(readString(), MeResponse::class.java)
        callback(response.you)
    }


}