import sbt._

class CodeFellowProject(info: ProjectInfo) extends DefaultProject(info) {

  import Configurations.{Compile, CompilerPlugin, Default, Provided, Runtime, Test}

  val bcel = "org.apache.bcel" % "bcel" % "5.2"

}


