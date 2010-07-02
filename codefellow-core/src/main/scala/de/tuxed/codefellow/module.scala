
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


case object StartCompiler
case object Shutdown
case object ReloadAllFiles
case class ReloadFile(file: String)
case class CompileAllFiles(errorFile: String)
case class CompleteMember(file: String, row: Int, column: Int, prefix: String)
case class CompleteScope(file: String, row: Int, column: Int, prefix: String)
//case class CompleteSmart(file: String, row: Int, column: Int, prefix: String)
case class TypeInfo(file: String, row: Int, column: Int)

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
      sender ! "reloading all files"
      startCompiler()
      compiler.reloadFiles(sourceFiles)
    }

    case ReloadFile(file) => {
      sender ! "reloading " + file
      startCompiler()
      compiler.reloadFiles(List(file))
    }

    case CompileAllFiles(errorFile) => {
      sender ! compiler.compileFiles(errorFile, sourceFiles)
    }

    case CompleteMember(file, row, column, prefix) => {
      startCompiler()
      sender ! compiler.completeMember(file, row, column, prefix)
    }

    case CompleteScope(file, row, column, prefix) => {
      startCompiler()
      sender ! compiler.completeScope(file, row, column, prefix)
    }

    /*
    case CompleteSmart(file, row, column, prefix) => {
      startCompiler()

      val line = Utils.getLineInFileThatContainsOffset(file, pos).getOrElse("").trim
      if (line == "" || line.endsWith("(") || line.endsWith(",") || line.endsWith("=>") || line.endsWith(";")) {
        sender ! compiler.completeScope(file, pos, prefix)
      } else {
        sender ! compiler.completeMember(file, pos, prefix)
      }
    }
    */

    case TypeInfo(file, row, column) => {
      startCompiler()
      sender ! compiler.typeInfo(file, row, column)
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
  }

  def compileFiles(errorFile: String, files: List[String]): String = {
    reloadFiles(files)

    var writer: java.io.BufferedWriter = null
    try {
      val file = new java.io.File(errorFile)
      writer = new java.io.BufferedWriter(new java.io.FileWriter(file, false))

      reporter.allNotes.foreach { n =>
        writer.write(n.file + ":")
        writer.write(n.line + ":")
        writer.write(n.col + ":")
        writer.write(n.msg)
        writer.write("\n")
      }

    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      writer.close()
    }
    errorFile
  }

  def completeMember(file: String, row: Int, column: Int, prefix: String): List[Map[String, Any]] = {
    val lines = Utils.getLinesFromFilePath(file)
    val offset = Utils.getWordBeforeCursorOffset(lines, row, column)

    val p = new OffsetPosition(getSourceFile(file), offset)
    val x = new Response[List[Member]]
    askTypeCompletion(p, x)

    val names = x.get match {
      case Left(m) => m
      case Right(e) => List()
    }
    val filtered = names filter {m =>
      m match {
        case TypeMember(sym, tpe, true, viaImport, viaView) => sym.nameString.startsWith(prefix)
        case _ => false
      }
    }
    filtered map {
      case TypeMember(sym, tpe, true, viaImport, viaView) => {
        Map("word" -> sym.nameString, 
            "abbr" -> (sym.nameString + tpe.toString),
            "icase" -> 0)
      }
    }
  }

  def completeScope(file: String, row: Int, column: Int, prefix: String): List[Map[String, Any]] = {
    val lines = Utils.getLinesFromFilePath(file)
    val offset = Utils.getWordBeforeCursorOffset(lines, row, column)

    val p = new OffsetPosition(getSourceFile(file), offset)
    val x = new Response[List[Member]]
    askScopeCompletion(p, x)

    val names = x.get match {
      case Left(m) => m
      case Right(e) => List()
    }
    val filtered = names filter {m =>
      m match {
        case ScopeMember(sym, tpe, true, viaImport) => sym.nameString.startsWith(prefix)
        case _ => false
      }
    }
    filtered map {
      case ScopeMember(sym, tpe, true, viaImport) => {
        Map("word" -> sym.nameString, 
            "abbr" -> (sym.nameString + " (" + viaImport + ")"),
            "icase" -> 0)
      }
    }
  }

  def typeInfo(file: String, row: Int, column: Int): String = {
    val lines = Utils.getLinesFromFilePath(file)
    val offset = Utils.getCursorOffset(lines, row, column)

    val p = new OffsetPosition(getSourceFile(file), offset)
    val x = new Response[Tree]()
    askTypeAt(p, x)
    val result = x.get match {
      case Left(tree) => typeOfTree(tree)
      case Right(e) => "UNKNOWN"
    }
    result
  }

  private def typeOfTree(t:Tree): String = {
    var tree = t
    println("Class of tree: " + tree.getClass)
    tree = tree match {
      case Select(qual, name) if tree.tpe == ErrorType => qual
      case t: ImplDef if t.impl != null => t.impl
      case t: ValOrDefDef if t.tpt != null => t.tpt
      case t: ValOrDefDef if t.rhs != null => t.rhs
      case t => t
    }
    if(tree.tpe != null) {
      tree.tpe.toString
    } else {
      "Could not determine type"
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
  }

}

