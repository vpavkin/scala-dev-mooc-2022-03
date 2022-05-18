package module1



import java.util.concurrent.Callable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object App {
  def main(args: Array[String]): Unit = {


    println(s"Hello from ${Thread.currentThread().getName}")


    def action(v: Int): Int = {
      Thread.sleep(1000)
      println(s"Action in ${Thread.currentThread().getName} -- $v")
      v
    }

    val ec = scala.concurrent.ExecutionContext.Implicits.global
    val ec2 = ExecutionContext.fromExecutor(executor.pool1)
    val ec3 = ExecutionContext.fromExecutor(executor.pool2)
    val ec4 = ExecutionContext.fromExecutor(executor.pool3)
    val ec5 = ExecutionContext.fromExecutor(executor.pool4)

//    def rates4 = {
//      val f1 = future.getRatesLocation1
//      val f2 = future.getRatesLocation2
//      for{
//        v1 <- f1
//        v2 <- f2
//      } yield v1 + v2
//    }
//    val r2: Future[(Int, Int)] =
//      future.getRatesLocation1.zip(future.getRatesLocation2)

//    val r = future.getRatesLocation1.flatMap{ i =>
//      Future.successful(i + 1)
//    }

//    future.getRatesLocation1.onComplete {
//      case Failure(exception) => println(exception.getMessage)
//      case Success(value) => println(value + 1)
//    }

//    val r3: Future[Int] = future.getRatesLocation1.recover{
//      case e => 0
//    }

//    val f1 = future.getRatesLocation1(ec2)
//    val f2 = future.getRatesLocation2(ec3)
//
//    val f3 = f1.flatMap{v1 =>
//      action(v1)
//      f2.map{ v2 =>
//        action(v2)
//      }(ec4)
//    }(ec5)


    println(promise.p1.isCompleted)
    println(promise.f1.isCompleted)
    promise.p1.complete(Try(action(20)))
    println(promise.p1.isCompleted)
    println(promise.f1.isCompleted)


    println("End of main method")

  }


}
