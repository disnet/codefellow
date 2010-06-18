package de.tuxed.codefellow.plugin

import sbt._
import java.io.{BufferedWriter, FileWriter}


trait CodeFellowPlugin extends Project {

  lazy val codefellow = task {
    handleProject(this)
    None
  }

  def handleProject(project: Project): Unit = project match {
    case parent: ParentProject => parent.dependencies.foreach(d => handleProject(d)) 
    case project: DefaultProject => createModuleDescription(project)
  }

  def createModuleDescription(project: DefaultProject) {
    val prj = Path.fromFile(project.info.projectDirectory)

    val libs = project.fullClasspath(new Configuration("compile")).get.toSeq
    val prjs = project.dependencies.toList.asInstanceOf[List[DefaultProject]].map(_.mainCompilePath)
    val all_jars = libs ++ prjs

    val src = project.mainScalaSourcePath
    val deps_src = project.dependencies.toList.asInstanceOf[List[DefaultProject]].map(_.mainScalaSourcePath)
    val all_srcs = src :: deps_src

    writeModuleFile(prj, project.name, all_srcs, all_jars)
  }

  def writeModuleFile(dir: Path, name: String, scalaSourcesPath: Seq[Path], classpath: Seq[Path]) {
    var writer: BufferedWriter = null
    try {
      val file = (dir / ".codefellow").asFile
      println("Writing CodeFellow file: " + file.getAbsolutePath)
      writer = new BufferedWriter(new FileWriter(file, false))
      writer.write(name + "\n")
      writer.write(scalaSourcesPath.mkString(":") + "\n")
      writer.write(classpath.mkString(":") + "\n")
    } catch {
      case _ => log.error("Error while creating .codefellow file for project " + name)
    } finally {
      writer.close()
    }
  }

}

