package module3


import module3.functional_effects.functionalProgram

object App {

  def main(args: Array[String]): Unit = {
    import functionalProgram.declarativeEncoding._
    // import functionalProgram.executableEncoding._

    interpret(p1)

  }
}
