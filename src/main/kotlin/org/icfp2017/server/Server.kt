@file:Suppress("ArrayInDataClass")

package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.icfp2017.*
import org.icfp2017.base.Score
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


typealias JSONString = String

data class MeRequest(val me: String)
data class MeResponse(val you: String)

data class ReadyRequest(
    val ready: PunterID,
    val state: JSONString
)

data class MoveResponse(
    val claim: Claim?,
    val pass: Pass?
)

data class MoveRequest(
    val claim: Claim?,
    val pass: Pass?,
    val state: JSONString
)

data class MovesArrayResponse(val moves: Array<MoveResponse>)

data class StopResponse(
    val moves: Array<MoveResponse>,
    val scores: Array<Score>
)

data class SetupResponse(
    val punter: PunterID,
    val punters: Int,
    val map: MapModel
)

data class MovesResponse(
    val move: MovesArrayResponse,
    val state: JSONString
)

data class StopGeneralResponse(
    val stop: StopResponse,
    val state: JSONString?
)

data class OnlineReadyRequest(val ready: PunterID)
data class OnlineMoveRequest(
    val claim: Claim?,
    val pass: Pass?
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
    private val gson = Gson()

    private var state: JSONString = "{}"

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
        val generalRequest = gson.fromJson<Map<String, Any>>(json, generalType)

        if (generalRequest.get("me") != null) {
            serverBehaviour.send(json)
            return
        }

        val potentialState = generalRequest.get("state")
        if (potentialState != null) {

            val readyRequest: ReadyRequest? = gson.fromJson(json, ReadyRequest::class.java)
            if (readyRequest != null && generalRequest.get("ready") != null) {
                val outJson = gson.toJson(OnlineReadyRequest(readyRequest.ready))
                state = readyRequest.state
                serverBehaviour.send(outJson)
                return
            }

            val moveRequest = gson.fromJson(json, MoveRequest::class.java)
            if (moveRequest != null) {
                val outJson = gson.toJson(OnlineMoveRequest(moveRequest.claim, moveRequest.pass))
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
        val response = gson.fromJson<Map<String, Any>>(json, generalType)

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
            val stateAsString = gson.toJson(state)
            val trimmedJSON = json.trim()
            val jsonWithState = trimmedJSON.substring(trimmedJSON.indices.first, trimmedJSON.indices.last) + ",\"state\":$stateAsString}"
            Logger.log("[Actual Sent] <--- $jsonWithState")
            return jsonWithState
        }

        return "{}"
    }

    private fun send(json: String) {
        Logger.log("[TCP] --> $json")
        val byteArray = json.toByteArray()
        val prefix = "${byteArray.size}:".toByteArray()
        outputStream.write(prefix)
        outputStream.write(byteArray)
        outputStream.flush()
    }


    private fun readString(): String {
        val size = readSize()
        val result = readBytes(size)
        Logger.log("[TCP] <-- $result")
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

    private val gson = Gson()

    fun me(me: PunterName, callback: (PunterName) -> Unit) {
        val me = gson.toJson(MeRequest(me))
        Logger.log(me)
        send(me)
        val response: MeResponse = gson.fromJson(readString(), MeResponse::class.java)
        callback(response.you)
    }
}