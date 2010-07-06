
package de.tuxed.codefellow

import java.io.File

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor

import scala.collection.mutable.ListBuffer

import scala.collection.mutable.{ HashMap, HashEntry, HashSet }
import scala.collection.mutable.{ ArrayBuffer, SynchronizedMap,LinkedHashMap }


case class Request(moduleIdentifierFile: String, message: AnyRef)

class ModuleRegistry(modules: List[Module]) extends Actor {

  def act {
  modules foreach { m =>
    println("Module: " + m.name + " in directory: " + m.path)
    m.start()
  }
  loop {
    receive {
      case Request(moduleIdentifierFile, message) => {

        sender ! {
          try {
            modules.find(m => moduleIdentifierFile.startsWith(m.path)) match {
              case Some(module) => Right(module !? message)
              case None => Left("File [" + moduleIdentifierFile + "] not part of the current project!")
            }
          } catch {
            case e: Exception => {
              e.printStackTrace
              sender ! Left("Exception :"+e.getMessage+"\n"+e.getStackTraceString)
            }
          }
        }

      } // case
    } // receive
  } // loop
  } // act
  
}
