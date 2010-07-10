
package de.tuxed.codefellow

import scala.actors._
import scala.actors.Actor._


case class Request(moduleIdentifierFile: String, message: AnyRef)

class ModuleRegistry(modules: List[Module]) extends Actor {

  def act {
    modules foreach { m =>
      println("Module: " + m.name + " in directory: " + m.path)
      m.start()
    }
    loop {
      try {
        receive {
          case Request(moduleIdentifierFile, message) => {
            modules.find { m => moduleIdentifierFile.startsWith(m.path) } match {
              case None => Left("File [" + moduleIdentifierFile + "] not part of the current project!")
              case Some(m) => sender ! Right(m !? message)
            }
          }
        }
      } catch {
        case e: Exception => {
          e.printStackTrace
          sender ! Left("Exception :"+e.getMessage+"\n"+e.getStackTraceString)
        }
      }
    }
  }

}

