
package de.tuxed.codefellow

import java.io.File

import scala.actors._
import scala.actors.Actor._

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
case class GetTypeAt(file: String, pos: Int)


class Module(name: String, scalaSourceDirs: Seq[String], classpath: Seq[String]) extends Actor {
    
  private var compiler: InteractiveCompiler = _

  private var sourceFiles: List[String] = Nil

  def act {
    loop {
      react {
        case StartCompiler => 
          createSourceFilesList
          createInteractiveCompiler
          reloadFiles

        case GetTypeAt(file, pos) =>
          Thread.sleep(2000)
          compiler.getTypeAt(file, pos)
      }
    }
  }

  private def createSourceFilesList() {
    val files = new ListBuffer[String]
    val handler: (File => Unit) = { dir =>
        for (f <- dir.listFiles) {
          if (f.getAbsolutePath.endsWith(".scala")) {
            println("ADDING:" + f.getAbsolutePath)
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

  private def reloadFiles() {
    compiler.blockingReloadFiles(sourceFiles)
  }

}


class InteractiveCompiler(settings:Settings, reporter:Reporter) extends Global(settings, reporter) {

  def blockingReloadFiles(files: List[String]) = {
    val x = new Response[Unit]()
    askReload(files.map(f => getSourceFile(f)), x)
    x.get
  }

  def getTypeAt(file: String, cursor: Int) = {
    val x = new Response[Tree]()
    val p = new OffsetPosition(getSourceFile(file), cursor)

    //askType(getSourceFile(file), false, x)
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

  def typeOfTree(t:Tree):Either[Type, Throwable] = {
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
    println("############################### RECOMPILING: " + units)
    super.recompile(units)
    println("############################### DONE: " + units)
  }

}




//object Main {
  //
  //  def main(args: Array[String]) {
    //    
    //    println("------------------------ start ---------------------")
    //    val compilerArgs = List(
      //      "-classpath",
      //      System.getProperty("java.class.path"),
      //      "-verbose")
    //
    //    val settings = new Settings(Console.println)
    //    settings.processArguments(compilerArgs, true)
    //
    //    val reporter = new ConsoleReporter(settings)
    //    val global = new MyCompiler(settings, reporter)
    //
    //    global.newRunnerThread
    //
    //    global.blockingReloadFile("/home/roman/Dateien/Projekte/workspace/vimscc/src/main/scala/tests/User.scala")
    //
    //    println("-----------")
    //
    //    println("WAIT");Thread.sleep(2000)
    //
    //    val t = global.getTypeAt("/home/roman/Dateien/Projekte/workspace/vimscc/src/main/scala/tests/User.scala", 72)
    //    val t2 = global.getTypeAt("/home/roman/Dateien/Projekte/workspace/vimscc/src/main/scala/tests/User.scala", 85)
    //
    //    println("------------------------ end ---------------------")
    //  }
    //
    //}
