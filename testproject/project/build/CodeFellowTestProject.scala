import sbt._

class CodeFellowTestProject(info: ProjectInfo)
  extends DefaultProject(info)
  with de.tuxed.codefellow.plugin.CodeFellowPlugin {

  lazy val print = task { log.info("This is a test."); None }

}


