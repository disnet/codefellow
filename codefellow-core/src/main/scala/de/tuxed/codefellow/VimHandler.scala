
package de.tuxed.codefellow

import java.io._
import java.net._

import scala.util.parsing.json.JSON

object VimSerializer {

  def toVimScript(input: Any): String = input match {
    case s: String => {
      "\"" + escapeStringForVim(s) + "\""
    }
    case i: Int => {
      i.toString
    }
    case d: Double => {
      d.toString
    }
    case l: List[Any] => {
      "[" + (l map { toVimScript } mkString ",") + "]"
    }
    case m: Map[Any, Any] => {
      "{" + (m map { case (k, v) => toVimScript(k) + ":" + toVimScript(v) } mkString ",") + "}"
    }
    case Left(l) => toVimScript(Map("left" -> l))
    case Right(l) => toVimScript(Map("right" -> l))
  }

  private def escapeStringForVim(string: String): String = {
    string
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
  }

}

class InputClosed extends Exception

abstract class VimJSONHandler(moduleRegistry: ModuleRegistry) {

  // open listens to the clients and returns replies.
  // It never returns unless you want the server to exit
  def open()

  // throws RuntimeException if json can't be parsed
  protected def createRequestFromJson(json: String): Request = {
    JSON.parseFull(json) match {
      case None => throw new RuntimeException("Request could not be parsed:" + json)
      case Some(map: Map[String, Any]) => {
        // Extract information
        val moduleIdentifierFile = map("moduleIdentifierFile").asInstanceOf[String]
        val messageType = map("message")
        val arguments = map("arguments").asInstanceOf[List[Any]]

        // Create Message instance
        val packageName = classOf[Module].getPackage.getName
        val fqcn = packageName + "." + messageType
        val clazz = classOf[Module].getClassLoader.loadClass(fqcn)
        val constructor = clazz.getDeclaredConstructors()(0)
        val parameterTypes = constructor.getParameterTypes
        val params = arguments.padTo(parameterTypes.size, "")

        // Type conversions, extend when necessary
        val typedParams: List[Any] = params.zip(parameterTypes).map {e =>
          if (e._2.equals(classOf[String])) e._1
          else if (e._2.equals(classOf[Int])) java.lang.Double.valueOf(e._1.toString).intValue
        }
      
        // Create instance
        val m = constructor.newInstance(typedParams.asInstanceOf[List[AnyRef]]: _*).asInstanceOf[AnyRef]
        Request(moduleIdentifierFile, m)
      }
    }
  }

  // returns String wich is parsed by Vim
  // String is one of
  // { 'right': result } indicating success or
  // { 'left': msg } passing the caught Exception to Vim
  def handleConnectionRequest(reader: BufferedReader) = {
    VimSerializer.toVimScript {
      try {
        var line = ""
        // Read lines until ENDREQUEST
        var tmp = ""
        while (tmp != "ENDREQUEST") {
          line += tmp
          tmp = reader.readLine()
          if (tmp == null)
            throw new InputClosed()
        }
        val request = createRequestFromJson(line)
        val reply   = moduleRegistry !? request
        reply

      } catch {
        case e: InputClosed =>
          throw e // is handled by caller (which calls System.exit in the StdIn case)
        case e: Throwable => {
          // Left("Exception: "+e+"\n"+e.getStackTraceString)
        }
      }
    }
  }

}


class VimHandlerTCPIP(moduleRegistry: ModuleRegistry) extends VimJSONHandler(moduleRegistry) {

  def open() {
    val port = 9081
    val listener = new ServerSocket(port)
    println("INFO: listening on TCP/IP port "+port)
    while (true) {
      try {
        val socket = listener.accept()
        // println("VimHandler: Connection start")
        val reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
	val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))

        out.write(handleConnectionRequest(new BufferedReader(reader)))
        out.flush()
        socket.close()
        // println("VimHandler: Connection end")
      }
      catch {
        case e: InputClosed =>
        {
          System.err.println("client disconnected unexpectedly")
        }
        case e: IOException => {
          System.err.println("Error in server listen loop: " + e)
        }
      }
    }
    listener.close()
  }

}


class VimHandlerStdinStdout(moduleRegistry: ModuleRegistry) extends VimJSONHandler(moduleRegistry) {

  def open() {
    println("INFO: listening on stdin")
    val reader = new BufferedReader(new InputStreamReader(System.in))
    while (true){
      try {
        val reply = handleConnectionRequest(reader)
        println("server:"+reply)
      } catch {
        case e: InputClosed => {
          println("input closed, shutting down")
          System.exit(1)
          "" // dummy - Scala doesn't know that exit(1) doesn't return
        }
      } finally {
        println("server:ENDREPLY")
      }
    }
  }

}
