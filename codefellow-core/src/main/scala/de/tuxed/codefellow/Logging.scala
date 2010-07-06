package de.tuxed.codefellow

object Logging {

  var debugging = false
  
  def logInfo(s: => String){
    println("INFO: "+s)
  }
  
  def logDebug(s: => String){
     if (debugging)
       println("DEUBG: "+s)
  }

  def logError(s: => String){
       System.err.println("ERROR: "+s)
  }
}

trait Logging {
  def logInfo(s: => String) {
    Logging.logInfo(s)
  }
  def logDebug(s: => String){
    Logging.logDebug(s)
  }
  def logError(s: => String){
    Logging.logError(s)
  }
}
