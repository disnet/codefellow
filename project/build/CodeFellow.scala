import sbt._

class CodeFellowParentProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val cf_core = project("codefellow-core", "codefellow-core", new CodeFellowCoreProject(_))
  lazy val cf_plugin = project("codefellow-plugin", "codefellow-plugin", new CodeFellowPluginProject(_),
    cf_core)

  class CodeFellowCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    val bcel = "org.apache.bcel" % "bcel" % "5.2"
  }

  class CodeFellowPluginProject(info: ProjectInfo) extends PluginProject(info) {
    //import Configurations.{Compile, CompilerPlugin, Default, Provided, Runtime, Test}
  }

}



