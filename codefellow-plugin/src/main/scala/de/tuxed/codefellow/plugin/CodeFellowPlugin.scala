package de.tuxed.codefellow.plugin

import sbt._


trait CodeFellowPlugin extends Project {

  lazy val codefellow = task { log.info("Hello World !"); None }

}

