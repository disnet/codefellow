package de.tuxed.codefellow.plugin

import sbt._
import java.io.{BufferedWriter, FileWriter}


trait CodeFellowPlugin extends Project {

  private val processed = new scala.collection.mutable.HashSet[Project]

  lazy val codefellow = task {
    handleProject(this)
    None
  }

  def handleProject(project: Project): Unit = {
    if (!processed.contains(project)) {
      processed += project
      project match {
        case parent: ParentProject => parent.dependencies.foreach(d => handleProject(d)) 
        case project: DefaultProject => createModuleDescription(project)
        case _ =>
      }
    }
  }

  def createModuleDescription(project: DefaultProject) {
    val prj = Path.fromFile(project.info.projectDirectory)

    val libs = project.fullClasspath(new Configuration("compile")).get.toSeq

    val deps = project.dependencies.toList
    val projectDependencies = deps flatMap {
      case project: DefaultProject => {
        handleProject(project)
        Some(project)
      }
      case parent: ParentProject => {
        handleProject(parent)
        None
      }
      case _ => None
    }
    val projectDependenciesCompilePaths = projectDependencies map { _.mainCompilePath }
    val projectDependenciesSourcePaths = projectDependencies map { _.mainScalaSourcePath }
    val all_jars = libs ++ projectDependenciesCompilePaths

    val src = project.mainScalaSourcePath
    val all_srcs = src :: projectDependenciesSourcePaths

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

