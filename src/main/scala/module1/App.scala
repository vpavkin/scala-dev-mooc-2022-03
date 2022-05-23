package module1



import module2.implicits.{implicit_conversions, implicit_scopes}

import java.util.concurrent.Callable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object App {
  def main(args: Array[String]): Unit = {


    println(s"Hello from ${Thread.currentThread().getName}")

    implicit_scopes.result

    // implicit_conversions


  }


}
