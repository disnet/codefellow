
package de.tuxed.codefellow

import java.io.File
import java.util.Scanner

import scala.collection.mutable.ListBuffer


object Launch {

  def main(args: Array[String]) {
    val root = if (args.size > 0) args(0) else "."
    val modules = findAllModules(root)
    val project = new Project(modules)
    val socketHandler = new SocketHandler(project)
    socketHandler.open()
    project.start()
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
        collected += parseModule(dir.getAbsolutePath, config)
      }
    }
    Utils.traverseDirectory(rootPath, filter, handler)
    collected.toList
  }

  def parseModule(moduleDir: String, config: File): Module = {
    val input = new Scanner(config)
    val name = input.nextLine
    val sources = input.nextLine.split(":")
    val classpath = input.nextLine.split(":")
    input.close
    new Module(name, new File(moduleDir).getCanonicalPath, sources, classpath)
  }

}

