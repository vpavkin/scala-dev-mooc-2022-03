package module1

import java.util.concurrent.{Callable, Future}

object App {
  def main(args: Array[String]): Unit = {


    println(s"Hello from ${Thread.currentThread().getName()}")

//    val t1 = new threads.Thread1
//    val t2 = new threads.Thread1
//
//    t1.start()
//    t1.join()
//    t2.start()

    def rates = {
      val t1 = threads.getRatesLocation1
      val t2 = threads.getRatesLocation2
      t1.start()
      t2.start()
      t1.join()
      t2.join()
    }

    def rates2 = {
      val t1: Int = threads.getRatesLocation3
      val t2: Int = threads.getRatesLocation4
      println(t1)
      println(t2)
    }

    def rates3 = {
      val t1 = threads.getRatesLocation5
      val t2 = threads.getRatesLocation6

      val t3: threads.ToyFuture[Int] = for{
        v1 <- t1
        v2 <- t2
      } yield v1 + v2

      t1.onComplete(i => println(s"Hello from future ${Thread.currentThread().getName} - $i"))
      t2.onComplete(i => println(s"Hello from future ${Thread.currentThread().getName} - $i"))
    }

   // threads.printRunningTime(rates)
   // threads.printRunningTime(rates2)
   // threads.printRunningTime(rates3)

    val runnable = new Runnable {
      override def run(): Unit = {
        Thread.sleep(1000)
        println(s"Hello from runnable - ${Thread.currentThread().getName}")
      }
    }
    val callable = new Callable[Int] {
      override def call(): Int = {
        Thread.sleep(1000)
        println("Hello from Callable")
        10 + 10
      }
    }

    // executor.pool3.execute(runnable)
    val f1: Future[Int] = executor.pool3.submit(callable)

   // val i: Int = f1.get()
  //  println(i)
    println("after future get")
    //executor.pool1.shutdown()
   Thread.sleep(2000)
  }
}
