
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

}

