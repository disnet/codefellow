
package de.tuxed.codefellow

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.Node


object Client {

  def main(args: Array[String]) {
    val node = new Node("localhost", 9051)
    val registry = RemoteActor.select(node, 'ModuleRegistry)
    val result = registry !? Request(args.toList)
    println(result.asInstanceOf[Seq[String]].mkString("\n"))
    System.exit(0)
  }

}
