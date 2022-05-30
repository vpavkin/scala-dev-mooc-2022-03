package module2

import cats.data.Chain
import module2.validation.UserDTO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


object App {

  def main(args: Array[String]): Unit = {
     println(validation.validateUserDataV2(UserDTO("", "", 10)))
     println(validation.validateUserDataV3(UserDTO("", "", 10)))
     println(validation.validateUserDataV4(UserDTO("", "", 10)))

    println(functional.f6.run("0"))

    // Invalid(Email invalidName invalid)
    // Invalid(Chain(Email invalid, Name invalid))

  }
}
