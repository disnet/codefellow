
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


case class Request(args: List[String])

class ModuleRegistry(modules: List[Module]) extends Actor {
  def act {
    modules.foreach(_.start)
    modules.foreach(_ ! StartCompiler) // TODO: Defer to improve startup time?
    RemoteActor.alive(9051)
    RemoteActor.register('ModuleRegistry, self)
    loop {
      try {
        receive {
          case Request(moduleIdentifierFile :: args) =>
            val module = modules.filter(m => moduleIdentifierFile.startsWith(m.path))(0)
            module !! StartCompiler
            val message = createMessage(args)
            println("REQUEST: Sending " + message + " to " + module + "")
            val result = module !? message
            println("REQUEST done")
            sender ! result
        }
      } catch {
        case e: Exception =>
          e.printStackTrace
          sender ! ""
      }
    }
  }

  private def createMessage(args: List[String]): AnyRef = {
    val packageName = getClass.getPackage.getName
    val fqcn = packageName + "." + args(0)
    val clazz = getClass.getClassLoader.loadClass(fqcn)
    val constructor = clazz.getDeclaredConstructors()(0)

    val parameterTypes = constructor.getParameterTypes
    val params = args.tail.padTo(parameterTypes.size, "")
    val typedParams: List[Any] = params.zip(parameterTypes).map {e =>
      // Type conversions, extend when necessary
      if (e._2.equals(classOf[String])) e._1
      else if (e._2.equals(classOf[Int])) e._1.toInt
    }
    constructor.newInstance(typedParams.asInstanceOf[List[AnyRef]]: _*).asInstanceOf[AnyRef]
  }
}

case object StartCompiler
case object Shutdown
case object ReloadAllFiles
case class ReloadFile(file: String)
case class CompleteMember(file: String, pos: Int, prefix: String)
case class CompleteScope(file: String, pos: Int, prefix: String)
case class GetTypeAt(file: String, pos: Int, prefix: String)

class Module(val name: String, val path: String, scalaSourceDirs: Seq[String], classpath: Seq[String]) extends Actor {
    
  private var compiler: InteractiveCompiler = _

  private var sourceFiles: List[String] = Nil

  val handler: PartialFunction[Any, Unit] = {
    case StartCompiler => 
      if (compiler == null) {
        createSourceFilesList
        createInteractiveCompiler
        compiler.reloadFiles(sourceFiles)
      }

    case Shutdown =>
      compiler.askShutdown
      compiler = null
      exit('stop)

    case ReloadAllFiles =>
      sender ! List("reloading files")
      compiler.reloadFiles(sourceFiles)

    case ReloadFile(file) =>
      sender ! compiler.reloadFiles(List(file))

    case CompleteMember(file, pos, prefix) =>
      sender ! compiler.completeMember(file, pos, prefix)

    case CompleteScope(file, pos, prefix) =>
      sender ! compiler.completeScope(file, pos, prefix)

    //case GetTypeAt(file, pos, prefix) =>
      //sender ! compiler.getTypeAt(file, pos, prefix)
  }

  val block: PartialFunction[Any, Any] = {
    case message: Any => {
      if (compiler != null)
        compiler.blockWhileActive
        message
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

    println("-------------------------------------------------")
    println("no of notes: " + reporter.allNotes.size)
    reporter.allNotes.foreach(println)
    println("-------------------------------------------------")

    List("files reloaded")
  }

  def completeMember(file: String, cursor: Int, prefix: String): List[String] = {
    println("COMPILER: complete member")

    reloadFiles(List(file))

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
    filtered.map {case TypeMember(sym, tpe, true, viaImport, viaView) => sym.nameString + "|" + tpe}
  }

  def completeScope(file: String, cursor: Int, prefix: String): List[String] = {
    println("COMPILER: complete scope")

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
    filtered.map {case ScopeMember(sym, tpe, true, viaImport) => sym.nameString}
  }

  def getTypeAt(file: String, cursor: Int) = {
    println("COMPILER: type at")
    val x = new Response[Tree]()
    val p = new OffsetPosition(getSourceFile(file), cursor)

    //askType(getSourceFile(file), false, x)
    //println(x.get)

    askTypeAt(p, x)
    x.get match {
      case Left(tree) =>
        println("TYPE:" + typeOfTree(tree))

      case Right(e:FatalError) =>
        println("FATAL ERROR:" + e)

      case Right(e) =>
        println("UNKNOWN:" + e)

    }
  }

  private def typeOfTree(t:Tree):Either[Type, Throwable] = {
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
      Left(tree.tpe)
    }
    else {
      Right(new Exception("Null tpe"))
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

