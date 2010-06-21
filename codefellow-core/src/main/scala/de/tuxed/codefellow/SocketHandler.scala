
package de.tuxed.codefellow

import java.util.Scanner
import java.io._
import java.net._

import scala.util.parsing.json.JSON


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
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
    try {
      var line = scanner.nextLine
      createRequestFromJson(line) match {
        case None => throw new RuntimeException("Request could not be parsed:" + line)
        case Some(request) => {
          println("GOT REQUEST:" + request)
          val result = project !? request
          println("RESULT:" + result)
          //out.write(result.toString + "\n")
          out.write("\n\nBLABLA\n")
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
    println("JSON parsing:" + json)
    JSON.parseFull(json) match {
      case None => None
      case Some(map: Map[String, Any]) => {
        // Extract information
        val moduleIdentifierFile = map("moduleIdentifierFile").asInstanceOf[String]
        val messageType = map("message")
        val arguments = map("arguments").asInstanceOf[List[Any]]

        // Create Message instance
        val packageName = classOf[Message].getPackage.getName
        val fqcn = packageName + "." + messageType
        val clazz = classOf[Message].getClassLoader.loadClass(fqcn)
        val constructor = clazz.getDeclaredConstructors()(0)
        val parameterTypes = constructor.getParameterTypes
        val params = arguments.padTo(parameterTypes.size, "")

        // Type conversions, extend when necessary
        val typedParams: List[Any] = params.zip(parameterTypes).map {e =>
          if (e._2.equals(classOf[String])) e._1
          else if (e._2.equals(classOf[Int])) java.lang.Double.valueOf(e._1.toString).intValue
        }
      
        // Create instance
        val m = constructor.newInstance(typedParams.asInstanceOf[List[AnyRef]]: _*).asInstanceOf[Message]
        Some(Request(moduleIdentifierFile, m))
      }
    }
  }

}

