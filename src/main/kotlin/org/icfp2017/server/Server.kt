@file:Suppress("ArrayInDataClass")

package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.icfp2017.*
import org.icfp2017.base.Score
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import com.google.gson.GsonBuilder




typealias JSONString = String

data class MeRequest(@SerializedName("me") val me: String)
data class MeResponse(@SerializedName("you") val you: String)

data class ServerMove(
    @SerializedName("move") val move: Move,
    @SerializedName("state") val state: String
)

data class ReadyRequest(
    @SerializedName("ready") val ready: PunterID,
    @SerializedName("state") val state: String?
)

data class MoveResponse(
    @SerializedName("claim") val claim: Claim?,
    @SerializedName("pass") val pass: Pass?
)

data class MoveRequest(
    @SerializedName("claim") val claim: Claim?,
    @SerializedName("pass") val pass: Pass?,
    @SerializedName("state") val state: String?
)


data class MovesArrayResponse(
    @SerializedName("moves") val move: Array<MoveResponse>
)

data class TimeoutResponse(val state: Any)

data class StopResponse(
    @SerializedName("moves") val moves: Array<MoveResponse>,
    @SerializedName("scores") val scores: Array<Score>
)

data class GeneralResponse(
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
    @SerializedName("state") val state: String?,

    //
    @SerializedName("you") val you: String?
)

data class GeneralRequest (
    @SerializedName("me") val me: String?
)


class OnlineServer(
    serverName: String = Arguments.server,
    serverPort: Int = Arguments.port,
    offlineServer: OfflineServer)
{

    private val serverBehaviour = ServerBehaviour({ json -> send(json) }, { readString() })

    private val client: Socket
    private val outputStream: OutputStream
    private val inputStream: InputStream
    private val offlineServer: OfflineServer

    private var state: String? = null

    init {
        val inteAddress = InetAddress.getByName(serverName)
        val socketAddress = InetSocketAddress(inteAddress, serverPort)
        client = Socket()
        val timeoutInMs = 10 * 1000
        client.connect(socketAddress, timeoutInMs)
        outputStream = client.outputStream
        inputStream = client.inputStream
        this.offlineServer = offlineServer

        offlineServer.serverBehaviour = ServerBehaviour(
            { json -> handleOfflineRequest(json) },
            { sendJsonToClient() }
        )

    }

    private fun handleOfflineRequest(json: JSONString) {

        Logger.log("[Proxiying] ---> $json")

        val generalType = object : TypeToken<Map<String, Any>>() {}.type
        val generalRequest = Gson().fromJson<Map<String, Any>>(json, generalType)

        if (generalRequest.get("me") != null) {
            serverBehaviour.send(json)
            return
        }



        val potentialState = generalRequest.get("state")
        if (potentialState != null) {

            val readyRequest: ReadyRequest? = Gson().fromJson(json, ReadyRequest::class.java)
            if (readyRequest != null) {
                val outJson = Gson().toJson(ReadyRequest(readyRequest.ready, null))
                state = readyRequest.state
                serverBehaviour.send(outJson)
                return
            }

            val moveRequest = Gson().fromJson(json, MoveRequest::class.java)
            if (moveRequest != null) {
                val outJson = Gson().toJson(MoveRequest(moveRequest.claim, moveRequest.pass, null))
                state = moveRequest.state
                serverBehaviour.send(outJson)
                return
            }
        }

        Logger.log("Aaaaaaaaa!")

    }

    private fun sendJsonToClient(): JSONString {

        val json = serverBehaviour.readString()
        Logger.log("[Proxiying] <--- $json")

        val generalType = object : TypeToken<Map<String, Any>>() {}.type
        val response = Gson().fromJson<Map<String, Any>>(json, generalType)

        //
        if (response.get("you") != null) {
            return json
        }

        // Setup
        if (response.get("punter") != null && response.get("punters") != null && response.get("map") != null) {
            return json
        }

        if (response.get("stop") != null) {
            return json
        }

        if (response.get("timeout") != null) {
            return json
        }

        if (response.get("move") != null) {
            val turnsType = object : TypeToken<GeneralResponse>() {}.type
            val response = Gson().fromJson<GeneralResponse>(json, turnsType)

            val moves = response.moves
            if (moves != null) {
                val updatedResponse = GeneralResponse(
                    punter = response.punter,
                    punters = response.punters,
                    moves = response.moves,
                    map = response.map,
                    stop = response.stop,
                    timeout = response.timeout,
                    you = response.you,
                    state = state
                )
                val outJson = Gson().toJson(updatedResponse)
                Logger.log("[Actual Sent] <--- $outJson")
                return outJson
            }
        }

        return "{}"
    }

    private fun send(json: String) {
        Logger.log(json)
        val byteArray = json.toByteArray()
        val prefix = "${byteArray.size}:".toByteArray()
        outputStream.write(prefix)
        outputStream.write(byteArray)
        outputStream.flush()
    }


    private fun readString(): String {
        val size = readSize()
        val result = readBytes(size)
        Logger.log(result)
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
        return string.toInt()
    }


}
class ServerBehaviour(val send: (JSONString) -> Unit, val readString: () -> JSONString)  {


    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        val me = Gson().toJson(MeRequest(me))
        Logger.log(me)
        send(me)
        val response: MeResponse = Gson().fromJson(readString(), MeResponse::class.java)
        callback(response.you)
    }


}