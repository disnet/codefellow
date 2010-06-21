
package de.tuxed.codefellow

import java.util.Scanner
import java.io._
import java.net._


class SocketHandler(project: Project) {

  def open() {
    val listener = new ServerSocket(9081)
    while (true) {
      try {
        val socket = listener.accept()
        println("SocketHandler: start")
        handleConnection(socket)
        println("SocketHandler: end")
      }
      catch {
        case e: IOException =>
        {
          System.err.println("Error in server listen loop: " + e)
        }
      }
    }
    listener.close()
  }

  def handleConnection(socket: Socket) {
    val scanner = new Scanner(new InputStreamReader(socket.getInputStream()))
    var line = scanner.nextLine
    println("GOT MESSAGE [" + line + "]")
    socket.close()
  }

}

