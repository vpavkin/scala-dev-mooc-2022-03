package module1

import module1.threads.async
import module1.utils.NameableThreads

import java.io.File
import java.util.{Timer, TimerTask}
import java.util.concurrent.{Callable, Executor, ExecutorService, Executors, ForkJoinPool, ThreadFactory, ThreadPoolExecutor}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, Promise, TimeoutException}
import scala.io.{BufferedSource, Source}
import scala.language.{existentials, postfixOps}
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

      val pool1: ExecutorService = Executors.newFixedThreadPool(2, NameableThreads("fixed-pool-1"))
      val pool2: ExecutorService = Executors.newCachedThreadPool(NameableThreads("cached-pool-2"))
      val pool3: ExecutorService = Executors.newWorkStealingPool(4)
      val pool4: ExecutorService = Executors.newSingleThreadExecutor(NameableThreads("singleThread-pool-4"))


}

object tryObj{


  def readFromFile(): List[Int] = {

    val s = Source.fromFile(new File("ints.txt"))

    val result = try {
      s.getLines().toList.map(_.toInt)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        Nil
    } finally {
      s.close()
    }
    result
  }

  def readFromFile2(): Try[List[String]] = {

    val s: Try[BufferedSource] = Try(Source.fromFile(new File("ints.txt")))
    def lines(s: Source): Try[List[String]] = Try(s.getLines().toList)

    val result: Try[List[String]] = for{
      src <- s
      l <- lines(src)
    } yield l

    s.foreach(_.close())

    result
  }

}


object future{
  def getRatesLocation1(implicit ec: ExecutionContext) = Future({
    Thread.sleep(1000)
    println(s"GetLocation1 - ${Thread.currentThread().getName}")
    10
  })

  def getRatesLocation2(implicit ec: ExecutionContext) = Future({
    Thread.sleep(2000)
    println(s"GetLocation2 - ${Thread.currentThread().getName}")
    20
  })

  def printFutureRunningTime[T](v: => Future[T])(implicit ec: ExecutionContext): Future[T] = {

    for{
      start <- Future.successful(System.currentTimeMillis())
      r <- v
      end <- Future.successful(System.currentTimeMillis())
      _ <- Future.successful(println(s"Execution time ${end -start}"))
    } yield r
  }

}


object promise{

  val p1: Promise[Int] = Promise[Int]
  val f1: Future[Int] = p1.future












  object FutureSyntax {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    def map[T, B](future: Future[T])(f: T => B): Future[B] = {
      val p = Promise[B]
      future.onComplete {
        case Failure(exception) => p.failure(exception)
        case Success(value) => p.success(f(value))
      }
      p.future
    }

    def flatMap[T, B](future: Future[T])(f: T => Future[B]): Future[B] = ???


    def make[T](v: => T)(implicit ec: ExecutionContext): Future[T] = {
      val p = Promise[T]
      val r = new Runnable {
        override def run(): Unit = {
          p.complete(Try(v))
        }
      }
      ec.execute(r)
      p.future
    }



    def make[T](v: => T, timeout: Long): Future[T] = {
      val p = Promise[T]
      val timer = new Timer(true)
      val timerTask = new TimerTask {
        override def run(): Unit = ???
      }
      timer.schedule(timerTask, timeout)
      ???

    }



  }


}