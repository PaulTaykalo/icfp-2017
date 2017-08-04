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


class OnlineServer: Server {


  private data class MeRequest(@SerializedName("me") val me: String)
  private data class MeResponse(@SerializedName("you") val you: String)

  private val client: Socket
  private val outputStream: OutputStream
  private val inputStream: InputStream

  init {
    // Parameteized
    val serverName = "punter.inf.ed.ac.uk"
    val serverPort = 9024

    val inteAddress = InetAddress.getByName(serverName)
    val socketAddress = InetSocketAddress(inteAddress, serverPort)
    client = Socket()
    val timeoutInMs = 10 * 1000
    client.connect(socketAddress, timeoutInMs)

    outputStream = client.outputStream
    inputStream = client.inputStream
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


  override fun me(me: PunterName, callback: (PunterName) -> Unit) {
    val request = Gson().toJson(MeRequest(me))
    send(request)

    val input = readString()
    print("Output is $input")
    val response: MeResponse = Gson().fromJson(input, MeResponse::class.java)
    callback(response.you)
  }

  override fun setup(callback: (Game) -> Void) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun ready(punterID: PunterID) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onMove(observer: (Array<Move>) -> Move) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onInterruption(callback: (String) -> Void) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onEnd(callback: (StopCommand) -> Void) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}