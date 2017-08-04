@file:Suppress("ArrayInDataClass")

package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.icfp2017.*
import org.icfp2017.base.Score
import org.icfp2017.base.StopCommand
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.InetSocketAddress
import java.net.InetAddress




interface Server {

  // Send me once we ready
  // me == Usernam
  fun me(me: PunterName, callback: (PunterName) -> Unit)

  // Send setup and wait for the callback
  fun setup(callback: (Game) -> Unit)

  // Send after setup completed
  fun ready(punterID: PunterID,
            onMove: (Array<Move>) -> Move,
            onInterruption: (String) -> Unit,
            onEnd: (StopCommand) -> Unit
  )

}

class OnlineServer(serverName: String = "punter.inf.ed.ac.uk", serverPort: Int = 9024) : Server {

  private data class MeRequest(@SerializedName("me") val me: String)
  private data class ReadyRequest(@SerializedName("ready") val ready: PunterID)
  private data class MeResponse(@SerializedName("you") val you: String)
  private data class MoveResponse(
      @SerializedName("claim") val claim: Claim?,
      @SerializedName("pass") val pass: Pass?
  )
  private data class MovesArrayResponse(
      @SerializedName("moves") val move: Array<MoveResponse>
  )

  private data class TimeoutResponse(val state: Any)

  private data class MoveArrayResponse(
      @SerializedName("move") val moves: MovesArrayResponse
  )

  private data class GeneralResponse(
      @SerializedName("move") val moves: MovesArrayResponse?,
      @SerializedName("stop") val stop: StopResponse?,
      @SerializedName("timeout") val timeout: TimeoutResponse?
  )

  private data class StopResponse(
      @SerializedName("moves") val moves: Array<MoveResponse>,
      @SerializedName("scores") val scores: Array<Score>
  )

  private val client: Socket
  private val outputStream: OutputStream
  private val inputStream: InputStream

  init {
    val inteAddress = InetAddress.getByName(serverName)
    val socketAddress = InetSocketAddress(inteAddress, serverPort)
    client = Socket()
    val timeoutInMs = 10 * 1000
    client.connect(socketAddress, timeoutInMs)
    outputStream = client.outputStream
    inputStream = client.inputStream
  }

  override fun me(me: PunterName, callback: (PunterName) -> Unit) {
    send(Gson().toJson(MeRequest(me)))
    val response: MeResponse = Gson().fromJson(readString(), MeResponse::class.java)
    callback(response.you)

  }

  override fun setup(callback: (Game) -> Unit) {
    val response: Game = Gson().fromJson(readString(), Game::class.java)
    println("Sent $response")
    callback(response)
  }


  override fun ready(punterID: PunterID, onMove: (Array<Move>) -> Move, onInterruption: (String) -> Unit, onEnd: (StopCommand) -> Unit) {
    send(Gson().toJson(ReadyRequest(punterID)))

    // Read potential command
    var timeoutsLeft = 10
    while (true) {
      val response: GeneralResponse = Gson().fromJson(readString(), GeneralResponse::class.java)
      val moves = response.moves
      if (moves != null) {
        val typedMoves: Array<Move> = moves.move.map {
          it.claim ?: it.pass ?: Pass(-1)
        }.toTypedArray()
        val move = onMove(typedMoves)
        send(Gson().toJson(move))
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

  private fun send(json: String) {
    println("-> $json")
    val byteArray = json.toByteArray()
    val prefix = "${byteArray.size}:".toByteArray()
    outputStream.write(prefix)
    outputStream.write(byteArray)
    outputStream.flush()
  }


  private fun readString(): String {
    val size = readSize()
    val result = readBytes(size)
    println("<-- $result")
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
    println("Is about to read $string")
    return string.toInt()
  }


}