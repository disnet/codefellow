
package de.tuxed.codefellow

import java.io.File
import java.util.Scanner

import scala.collection.mutable.ListBuffer


object Launch {

  def main(args: Array[String]) {
    val root = if (args.size > 0) args(0) else "."
    val modules = findAllModules(root)
    //modules.foreach {m =>
      //m.start
      //m ! StartCompiler
    //}

    modules(0).start
    modules(0) ! StartCompiler
    modules(0) ! GetTypeAt("/home/roman/Dateien/Projekte/workspace/codefellow/testproject/project2/src/main/scala/Project2.scala", 151)
  }

  def findAllModules(rootPath: String): List[Module] = {
    val collected = new ListBuffer[Module]
    val filter = { dir: File =>
      val n = dir.getAbsolutePath
      !n.endsWith("src") && !n.endsWith("target")
    }
    val handler: (File => Unit) = { dir =>
      val config = new File(dir.getAbsolutePath + "/" + ".codefellow")
      if (config.exists) {
        println("ADDING PROJECT:" + config.getAbsolutePath)
        collected += parseModule(config)
      }
    }
    Utils.traverseDirectory(rootPath, filter, handler)
    collected.toList
  }

  def parseModule(config: File): Module = {
    val input = new Scanner(config)
    val name = input.nextLine
    val sources = input.nextLine.split(":")
    val classpath = input.nextLine.split(":")
    input.close
    new Module(name, sources, classpath)
  }

}

