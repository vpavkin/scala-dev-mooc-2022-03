package module1

import java.util.concurrent.{Callable, Executor, ExecutorService, Executors, ForkJoinPool, ThreadPoolExecutor}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object threads {


  def printRunningTime(v: => Unit): Unit = {
    val start = System.currentTimeMillis()
    v
    val end = System.currentTimeMillis()
    println(s"Execution time ${end -start}")
  }

  def getRatesLocation1: Thread = async({
    Thread.sleep(1000)
    println("GetLocation1")
  })

  def getRatesLocation2: Thread = async({
    Thread.sleep(2000)
    println("GetLocation2")
  })

  def async(f: => Unit): Thread = {
    new Thread{
      override def run(): Unit = f
    }
  }

  def async2[A](f: => A): A = {
    var v: A = null.asInstanceOf[A]

    val t = new Thread{
      override def run(): Unit = {
        v = f
      }
    }
    t.start()
    t.join()
    v
  }

  def getRatesLocation3: Int = async2({
    Thread.sleep(1000)
    println("GetLocation1")
    10
  })

  def getRatesLocation4: Int = async2({
    Thread.sleep(2000)
    println("GetLocation2")
    20
  })


  class ToyFuture[T] private (v: () => T){

    private var isCompleted: Boolean = false
    private var r: T = null.asInstanceOf[T]
    private val q = mutable.Queue[T => _]()


    def map[B](f: T => B): ToyFuture[B] = ???
    def flatMap[B](f: T => ToyFuture[B]): ToyFuture[B] = ???

    def onComplete[U](f: T => U): Unit = {
      if(isCompleted) f(r)
      else q.enqueue(f)
    }

    private def start() = {
      val t = new Thread{
        override def run(): Unit = {
          val result = v()
          r = result
          isCompleted = true
          while (q.nonEmpty){
            q.dequeue()(result)
          }
        }
      }
      t.start()
    }
  }

  object ToyFuture {
    def apply[T](v: => T): ToyFuture[T] = {
      val f = new ToyFuture[T](() => v)
      f.start()
      f
    }
  }

  def getRatesLocation5: ToyFuture[Int] = ToyFuture({
    Thread.sleep(1000)
    println("GetLocation1")
    10
  })

  def getRatesLocation6: ToyFuture[Int] = ToyFuture({
    Thread.sleep(2000)
    println("GetLocation2")
    20
  })





  class Thread1 extends Thread {
    override def run(): Unit = {
      Thread.sleep(1000)
      println(s"Hello from ${Thread.currentThread().getName()}")
    }
  }

}







object executor {

      val pool1: ExecutorService = Executors.newFixedThreadPool(2)
      val pool2: ExecutorService = Executors.newCachedThreadPool()
      val pool3: ExecutorService = Executors.newWorkStealingPool(4)


}


object future{

  import scala.concurrent.ExecutionContext.Implicits.global

  val f1 = Future(2 + 2)
  val f2 = Future.failed(new Exception("Ooops"))
  val f3 = Future.successful(2 + 2)
  val f4 = Future.fromTry(Try(1 / 0))

  val v = f1.map(i => i + 1)

  for{
    v1 <- Future(2 + 2)
    v2 <- Future(2 + 2)
  } yield v1 + v2

  val r: Unit = f1.onComplete {
    case Failure(exception) => println(exception.getMessage)
    case Success(value) => value + 1
  }

  val r2: Int = Await.result(f1, 5 second)
  val r3: Future[Int] = Await.ready(f1, 5 second)





}