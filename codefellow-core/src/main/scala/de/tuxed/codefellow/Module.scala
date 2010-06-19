
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
    modules.foreach(_ ! StartCompiler) // TODO: May better defer
    RemoteActor.alive(9051)
    RemoteActor.register('ModuleRegistry, self)
    loop {
      try {
        receive {
          case Request(moduleIdentifierFile :: args) =>
            val module = modules.filter(m => moduleIdentifierFile.startsWith(m.path))(0)
            module !! StartCompiler
            val message = createMessage(args)
            val result = module !? message
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

    val typedArgs: List[Any] = args.tail.zip(constructor.getParameterTypes).map {e =>
      // Type conversions, extend when necessary
      if (e._2.equals(classOf[String])) e._1
      else if (e._2.equals(classOf[Int])) e._1.toInt
    }
    constructor.newInstance(typedArgs.asInstanceOf[List[AnyRef]]: _*).asInstanceOf[AnyRef]
  }
}

case object StartCompiler
case object Shutdown
case object ReloadAllFiles
case class ReloadFile(file: String)
case class GetTypeAt(file: String, pos: Int, prefix: String)
case class CompleteScope(file: String, pos: Int, prefix: String)
case class CompleteType(file: String, pos: Int, prefix: String)

class Module(val name: String, val path: String, scalaSourceDirs: Seq[String], classpath: Seq[String]) extends Actor {
    
  private var compiler: InteractiveCompiler = _

  private var sourceFiles: List[String] = Nil

  def act {
    loop {
      receive {
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
          sender ! compiler.reloadFiles(sourceFiles)

        case ReloadFile(file) =>
          sender ! compiler.reloadFile(file)

        case GetTypeAt(file, pos, prefix) =>
          sender ! compiler.getTypeAt(file, pos, prefix)

        case CompleteScope(file, pos, prefix) =>
          sender ! compiler.completeScope(file, pos, prefix)

        case CompleteType(file, pos, prefix) =>
          sender ! compiler.completeType(file, pos, prefix)
      }
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
    val reporter = new ConsoleReporter(settings)
    compiler = new InteractiveCompiler(settings, reporter)
    compiler.newRunnerThread
  }

}


class InteractiveCompiler(settings:Settings, reporter:Reporter) extends Global(settings, reporter) {

  def reloadFile(file: String) {
    val x = new Response[Unit]
    scheduler postWorkItem new WorkItem {
      def apply() = respond(x)(reloadSources(List(getSourceFile(file))))
      override def toString = "quickReload " + file
    }
    x.get
  }

  def reloadFiles(files: List[String]) = {
    val x = new Response[Unit]
    askReload(files.map(f => getSourceFile(f)), x)
    x.get
    Thread.sleep(2000) // FIXME: Timeout is required right now.
  }

  def completeScope(file: String, cursor: Int, prefix: String): List[String] = {
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

  def completeType(file: String, cursor: Int, prefix: String): List[Member] = {
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

    println("#############-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#")
    for (m <- names) {
      println(m)
    }
    println("#############-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#")
    filtered
  }

  def getTypeAt(file: String, cursor: Int, prefix: String) = {
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

}

