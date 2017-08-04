package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.icfp2017.Game
import org.icfp2017.Move
import org.icfp2017.PunterID
import org.icfp2017.PunterName
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
  fun ready(punterID: PunterID)

  fun onMove(observer: (Array<Move>) -> Move)

  // Subscribe if you want to stop any calcution
  fun onInterruption(callback: (String) -> Unit)

  ///
  fun onEnd(callback: (StopCommand) -> Unit)

}


class OnlineServer(serverName: String = "punter.inf.ed.ac.uk", serverPort: Int = 9024) : Server {

  private data class MeRequest(@SerializedName("me") val me: String)
  private data class ReadyRequest(@SerializedName("ready") val ready: PunterID)
  private data class MeResponse(@SerializedName("you") val you: String)

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
    println("Sent $response")

    callback(response.you)

  }

  override fun setup(callback: (Game) -> Unit) {
    val response: Game = Gson().fromJson(readString(), Game::class.java)
    println("Sent $response")
    callback(response)
  }

  override fun ready(punterID: PunterID) {
    send(Gson().toJson(ReadyRequest(punterID)))
  }

  override fun onMove(observer: (Array<Move>) -> Move) {

  }

  override fun onInterruption(callback: (String) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onEnd(callback: (StopCommand) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun send(json: String) {
    val byteArray = json.toByteArray()
    val prefix = "${byteArray.size}:".toByteArray()
    outputStream.write(prefix)
    outputStream.write(byteArray)
    outputStream.flush()
  }


  private fun readString(): String {
    val size = readSize()
    val result = readBytes(size)
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
    print("Is about to read $string")
    return string.toInt()
  }


}