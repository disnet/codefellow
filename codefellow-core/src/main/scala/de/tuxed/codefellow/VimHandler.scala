
package de.tuxed.codefellow

import java.util.Scanner
import java.io._
import java.net._

import scala.util.parsing.json.JSON


class VimHandler(moduleRegistry: ModuleRegistry) {

  def open() {
    val listener = new ServerSocket(9081)
    while (true) {
      try {
        val socket = listener.accept()
        println("VimHandler: Connection start")
        handleConnection(socket)
        println("VimHandler: Connection end")
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
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
    try {
      var line = ""
      // Read lines until ENDREQUEST
      var tmp = ""
      while (tmp != "ENDREQUEST") {
        line += tmp
        while (!scanner.hasNextLine) {
          Thread.sleep(50)
        }
        tmp = scanner.nextLine
      }

      //println("VimHandler: Request [" + line + "]")
      createRequestFromJson(line) match {
        case None => throw new RuntimeException("Request could not be parsed:" + line)
        case Some(request) => {
          val result = moduleRegistry !? request
          val forVim = createVimScript(result.asInstanceOf[List[String]].mkString("\n"))
          //println("VimHandler: Response [" + forVim + "]")
          out.write(forVim)
          out.flush()
        }
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      socket.close()
    }
  }

  private def createRequestFromJson(json: String): Option[Request] = {
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

  private def createVimScript(string: String): String = {
    string.replace("\"", "\\\"")
  }

}

