package de.tuxed.codefellow.testproject2


object Project2 {
  var name = "Roman"

  def test1(arg: String) = {
    arg.substring(1) 
    val number = 1

    1
  }

  def test2() {
    val res = test1("abc")
    println(res)
    val list = List(1, 2, 3)
    val b = list match {
      case x :: xs => x.byteValue
    }
    println(b)

  }

}




