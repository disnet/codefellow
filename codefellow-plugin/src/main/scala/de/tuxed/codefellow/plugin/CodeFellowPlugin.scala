package de.tuxed.codefellow.plugin

import sbt._

import de.tuxed.codefellow.Main


trait CodeFellowPlugin extends Project {

  lazy val codefellow = task {
    log.info("Hello World !")

    log.info(classOf[Main].toString)

    None
  }

}

