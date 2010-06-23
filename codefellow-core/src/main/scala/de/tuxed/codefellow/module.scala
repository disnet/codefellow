
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


sealed class Message
case object StartCompiler extends Message
case object Shutdown extends Message
case object ReloadAllFiles extends Message
case class ReloadFile(file: String) extends Message
case class CompleteMember(file: String, pos: Int, prefix: String) extends Message
case class CompleteScope(file: String, pos: Int, prefix: String) extends Message
case class TypeInfo(file: String, pos: Int) extends Message

class Module(val name: String, val path: String, scalaSourceDirs: Seq[String], classpath: Seq[String]) extends Actor {
    
  private var compiler: InteractiveCompiler = _

  private var sourceFiles: List[String] = Nil

  val block: PartialFunction[Any, Any] = {
    case message: Any => {
      if (compiler != null) {
        compiler.blockWhileActive
      }
      message
    }
  }

  val handler: PartialFunction[Any, Unit] = {
    case StartCompiler => {
      startCompiler()
    }

    case Shutdown => {
      if (compiler != null) {
        compiler.askShutdown
        compiler = null
      }
      exit('stop)
    }

    case ReloadAllFiles => {
      sender ! List("reloading files")
      startCompiler()
      compiler.reloadFiles(sourceFiles)
    }

    case ReloadFile(file) => {
      sender ! List("reloading file")
      startCompiler()
      compiler.reloadFiles(List(file))
    }

    case CompleteMember(file, pos, prefix) => {
      startCompiler()
      sender ! compiler.completeMember(file, pos, prefix)
    }

    case CompleteScope(file, pos, prefix) => {
      startCompiler()
      sender ! compiler.completeScope(file, pos, prefix)
    }

    case TypeInfo(file, pos) => {
      startCompiler()
      sender ! compiler.typeInfo(file, pos)
    }
  }

  def act {
    loop {
      receive(block andThen handler)
    }
  }

  private def createSourceFilesList() {
    val files = new ListBuffer[String]
    val handler: (File => Unit) = { dir =>
        for (f <- dir.listFiles) {
          if (f.getAbsolutePath.endsWith(".scala")) {
            files += f.getAbsolutePath
          }
      }
    }
    scalaSourceDirs.foreach(d => Utils.traverseDirectory(d, _ => true, handler))
    sourceFiles = files.toList
  }

  private def createInteractiveCompiler() {
    val cp = System.getProperty("java.class.path") + ":" + classpath.mkString(":")
    val compilerArgs = List("-classpath", cp, "-verbose")
    val settings = new Settings(Console.println)
    settings.processArguments(compilerArgs, true)
    //val reporter = new ConsoleReporter(settings)
    val reporter = new PresentationReporter
    compiler = new InteractiveCompiler(settings, reporter)
    compiler.newRunnerThread
  }

  private def startCompiler() {
    if (compiler == null) {
      createSourceFilesList
      createInteractiveCompiler
      compiler.reloadFiles(sourceFiles)
    }
  }

  override def toString = "Module(" + name + ")"

  override def hashCode = name.hashCode * path.hashCode

}


class InteractiveCompiler(settings: Settings, reporter: PresentationReporter) extends Global(settings, reporter) {

  var active = false

  var numberOfRuns: Long = 0

  def blockWhileActive() {
    while (active) {
      println("BLOCKING compiler is active")
      Thread.sleep(50)
    }
  }

  def unitOfWork[A](body: => A): A = {
    val start = numberOfRuns
    val result = body
    while (start == numberOfRuns) {
      println("BLOCKING numberOfRuns did not change")
      Thread.sleep(50)
    }
    result
  }

  def reloadFiles(files: List[String]) = unitOfWork {
    val x = new Response[Unit]
    askReload(files.map(f => getSourceFile(f)), x)
    println(x.get)
    List("files reloaded")
  }

  def completeMember(file: String, cursor: Int, prefix: String): List[String] = {
    println("COMPILER: complete member")

    //reloadFiles(List(file))

    val x = new Response[List[Member]]
    val p = new OffsetPosition(getSourceFile(file), cursor)
    askTypeCompletion(p, x)

    val names = x.get match {
      case Left(m) => m
      case Right(e) => List()
    }
    val filtered = names filter {m =>
      m match {
        case TypeMember(sym, tpe, true, viaImport, viaView) =>
          if (sym.nameString.startsWith(prefix)) true else false
        case _ =>
          false
      }
    }
    filtered map { case TypeMember(sym, tpe, true, viaImport, viaView) => sym.nameString + ";" + tpe }
  }

  def completeScope(file: String, cursor: Int, prefix: String): List[String] = {
    println("COMPILER: complete scope")

    //reloadFiles(List(file))

    val x = new Response[List[Member]]
    val p = new OffsetPosition(getSourceFile(file), cursor)
    askScopeCompletion(p, x)

    val names = x.get match {
      case Left(m) => m
      case Right(e) => List()
    }
    val filtered = names filter {m =>
      m match {
        case ScopeMember(sym, tpe, true, viaImport) =>
          if (sym.nameString.startsWith(prefix)) true else false
        case _ =>
          false
      }
    }
    filtered map { case ScopeMember(sym, tpe, true, viaImport) => sym.nameString + ";" + viaImport }
  }

  def typeInfo(file: String, cursor: Int) = {
    println("COMPILER: typeinfo")
    val x = new Response[Tree]()
    val p = new OffsetPosition(getSourceFile(file), cursor)

    //askType(getSourceFile(file), false, x)
    //println(x.get)

    askTypeAt(p, x)
    val result = x.get match {
      case Left(tree) =>
        typeOfTree(tree)

      case Right(e:FatalError) =>
        "FATAL ERROR"

      case Right(e) =>
        "UNKNOWN"
    }
    List(result)
  }

  private def typeOfTree(t:Tree): String = {
    var tree = t
    println("Class of tree: " + tree.getClass)
    tree = tree match {
      case Select(qual, name) if tree.tpe == ErrorType =>
      {
        qual
      }
      case t:ImplDef if t.impl != null =>
      {
        t.impl
      }
      case t:ValOrDefDef if t.tpt != null =>
      {
        t.tpt
      }
      case t:ValOrDefDef if t.rhs != null =>
      {
        t.rhs
      }
      case t => t
    }
    if(tree.tpe != null) {
      tree.tpe.toString
    } else {
      "UNKNOWN"
    }
  }

   override def recompile(units: List[RichCompilationUnit]) {
    println("RECOMPILING start: " + units)
    try {
      active = true
      super.recompile(units)
    } catch {
      case e: Throwable =>
        println("RECOMPILING error: " + units)
        throw e
    } finally {
      println("RECOMPILING finally: " + units)
      active = false
      numberOfRuns += 1
    }

    println("-------------------------------------------------")
    println("no of notes: " + reporter.allNotes.size)
    reporter.allNotes.foreach(println)
    println("-------------------------------------------------")
  }

}

