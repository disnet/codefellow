
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


abstract class VimJSONHandler(moduleRegistry: ModuleRegistry) {

  // open listens to the clients and returns replies.
  // It never returns unless you want the server to exit
  def open()

  protected def createRequestFromJson(json: String): Option[Request] = {
    JSON.parseFull(json) match {
      case None => None
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
        Some(Request(moduleIdentifierFile, m))
      }
    }
  }

}


class VimHandlerTCPIP(moduleRegistry: ModuleRegistry) extends VimJSONHandler(moduleRegistry) {

  def open() {
    val listener = new ServerSocket(9081)
    while (true) {
      try {
        val socket = listener.accept()
        //println("VimHandler: Connection start")
        handleConnection(socket)
        //println("VimHandler: Connection end")
      }
      catch {
        case e: IOException => {
          System.err.println("Error in server listen loop: " + e)
        }
      }
    }
    listener.close()
  }

  def handleConnection(socket: Socket) {
    val reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
    try {
      var line = ""
      // Read lines until ENDREQUEST
      var tmp = ""
      while (tmp != "ENDREQUEST") {
        line += tmp
        tmp = reader.readLine()
      }

      //println("VimHandler: Request [" + line + "]")
      createRequestFromJson(line) match {
        case None => throw new RuntimeException("Request could not be parsed:" + line)
        case Some(request) => {
          val result = moduleRegistry !? request
          val forVim = VimSerializer.toVimScript(result)
          //println("VimHandler: Response >>>" + forVim + "<<<")
          out.write(forVim)
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        out.write("Exception: " + e)
      }
    } finally {
      out.flush()
      socket.close()
    }
  }

}

