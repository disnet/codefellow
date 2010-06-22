
package de.tuxed.codefellow

import java.io.File

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor

import scala.collection.mutable.ListBuffer

import scala.tools.nsc.interactive.{Global, CompilerControl}
import scala.tools.nsc.{Settings, FatalError}
import scala.tools.nsc.reporters.{Reporter, ConsoleReporter}
import scala.tools.nsc.util.{ClassPath, MergedClassPath, SourceFile, Position, OffsetPosition, NoPosition}
import scala.collection.mutable.{ HashMap, HashEntry, HashSet }
import scala.collection.mutable.{ ArrayBuffer, SynchronizedMap,LinkedHashMap }
import scala.tools.nsc.symtab.Types
import scala.tools.nsc.symtab.Flags


case class Request(moduleIdentifierFile: String, message: Message)

class ModuleRegistry(modules: List[Module]) extends Actor {

  def act {
    modules foreach { _.start }
    //modules foreach { _ ! StartCompiler } // TODO: Defer to improve startup time?
    loop {
      try {
        receive {
          case Request(moduleIdentifierFile, message) => {
            // TODO Check if a module was found
            val selected = modules.filter(m => moduleIdentifierFile.startsWith(m.path))
            if (selected.size != 0) {
              val result = selected(0) !? message
              sender ! result
            } else {
              println("File [" + moduleIdentifierFile + "] not part of the current project!")
              sender ! List("")
            }
          }
        }
      } catch {
        case e: Exception => {
          e.printStackTrace
          sender ! List("")
        }
      }
    }
  }
  
}
