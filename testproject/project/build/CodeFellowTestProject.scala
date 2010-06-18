import sbt._

class CodeFellowTestProject(info: ProjectInfo)
extends ParentProject(info)
with de.tuxed.codefellow.plugin.CodeFellowPlugin {

  lazy val test_project1 = project("project1", "project1", new CodeFellowTestProject1(_))
  lazy val test_project2 = project("project2", "project2", new CodeFellowTestProject2(_), test_project1)

  class CodeFellowTestProject1(info: ProjectInfo) extends DefaultProject(info) {
    val commons_logging = "commons-logging" % "commons-logging" % "1.1.1" % "compile"
  }

  class CodeFellowTestProject2(info: ProjectInfo) extends DefaultProject(info) {
    def guiceyFruitRepo = "GuiceyFruit Repo" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
    val guicey = "org.guiceyfruit" % "guice-all" % "2.0" % "compile"
  }

}


