
package de.tuxed.codefellow

import java.io.File


object Utils {

  def traverseDirectory(start: String, dirFilter: (File) => Boolean, handler: (File) => Unit) {
    val f = new File(start)
    if (f.listFiles != null) {
      handler(f)

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
      if (counted > offset) return Some(line)
    }
    None
  }

  def getLinesFromFilePath(path: String): List[String] =
    io.Source.fromFile(path).getLines.toList

  def getLineOffset(lines: List[String], row: Int): Int =
    lines.take(row).map(_.length +1).sum

  def getCursorOffset(lines: List[String], row: Int, column: Int): Int =
    getLineOffset(lines, row) + column

  def getWordBeforeCursorOffset(lines: List[String], row: Int, column: Int): Int = {
    val reversed = lines(row).substring(0, column - 1).reverse
    val afterWord = reversed dropWhile { c => c != ' ' && c != '.' } // Find invocation char
    val afterInvo = afterWord dropWhile { c => c == ' ' || c == '.' } // Find word start
    afterInvo.mkString.length + getLineOffset(lines, row)
  }

}

