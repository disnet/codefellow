import sbt._

class CodeFellowParentProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val cf_core = project("codefellow-core", "codefellow-core", new CodeFellowCoreProject(_))
  lazy val cf_plugin = project("codefellow-plugin", "codefellow-plugin", new CodeFellowPluginProject(_),
    cf_core)

  class CodeFellowCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    val bcel = "org.apache.bcel" % "bcel" % "5.2"
    override def managedStyle = ManagedStyle.Maven
  }

  class CodeFellowPluginProject(info: ProjectInfo) extends PluginProject(info) {
    val core = "de.tuxed" % "codefellow-core" % "1.0"
    override def managedStyle = ManagedStyle.Maven
  }

}



