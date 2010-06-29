
package de.tuxed.codefellow

import java.io.File


object Utils {

  def traverseDirectory(start: String, dirFilter: (File) => Boolean, handler: (File) => Unit) {
    val f = new File(start)
    if (f.listFiles != null)
      handler(f)

    if (f.listFiles != null) {
      for (entry <- f.listFiles) {
        if (entry.isDirectory && dirFilter(entry)) {
          traverseDirectory(entry.getAbsolutePath, dirFilter, handler)
        }
      }
    }
  }

  def getLineInFileThatContainsOffset(file: String, offset: Int): Option[String] = {
    val lines = io.Source.fromFile(file).getLines()
    var counted = 0
    for (line <- lines) {
      counted += line.size + 1
      if (counted >= offset) return Some(line)
    }
    None
  }

}

