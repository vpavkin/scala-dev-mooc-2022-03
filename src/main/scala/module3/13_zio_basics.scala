package module3

import zio.clock.{Clock, nanoTime}
import zio.console.{Console, getStrLn}

import java.io.IOException
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Try
import zio.duration._
import scala.language.postfixOps
import zio.Task
import zio.IO
import zio.RIO
import zio.URIO
import zio.UIO
import zio.ZIO



/** **
 * ZIO[-R, +E, +A] ----> R => Either[E, A]
 *
 */


object toyModel {


  /**
   * Используя executable encoding реализуем свой zio
   */

  case class ZIO[-R, +E, +A](run: R => Either[E, A]){ self =>

    def map[B](f: A => B): ZIO[R, E, B] =
      ZIO(r => self.run(r).map(f))

    def flatMap[R1 <: R, E1 >: E, B](f: A => ZIO[R1, E1, B]): ZIO[R1, E1, B]  =
      ZIO(r => self.run(r).fold(e => ZIO.fail(e), a => f(a)).run(r))
  }


  /**
   * Реализуем конструкторы под названием effect и fail
   */

  object ZIO{

    def effect[A](a: => A): ZIO[Any, Throwable, A] = try{
      ZIO(_ => Right(a))
    } catch {
      case e: Throwable => fail(e)
    }

    def fail[E](e: => E): ZIO[Any, E, Nothing] =
      ZIO(_ => Left(e))
  }



  /** *
   * Напишите консольное echo приложение с помощью нашего игрушечного ZIO
   */

   lazy val echo: ZIO[Any, Throwable, Unit] = for{
     str <- ZIO.effect(StdIn.readLine())
     _ <- ZIO.effect(println(str))
   } yield ()




  type Error
  type Environment

  type F[A] = ZIO[Any, Throwable, A]




  lazy val _: Task[Int] = ??? // ZIO[Any, Throwable, Int]
  lazy val _: IO[Error, Int] = ??? // ZIO[Any, Error, Int]
  lazy val _: RIO[Environment, Int] = ??? // ZIO[Environment, Throwable, Int]
  lazy val _: URIO[Environment, Int] = ??? // ZIO[Environment, Nothing, Int]
  lazy val _: UIO[Int] = ??? // ZIO[Any, Nothing, Int]
}

object zioConstructors {


  // константа
  val z1: UIO[Int] = ZIO.succeed(7)


  // любой эффект
  val z2: Task[Unit] = ZIO.effect(println("Hello world"))

  // любой не падающий эффект

  val z3: UIO[Unit] = ZIO.effectTotal(println("Hello"))




  // From Future
  val f: Future[Int] = ???
  val z4: Task[Int] = ZIO.fromFuture(_ => f)


  // From try
  lazy val t: Try[String] = ???
  lazy val z5: Task[String] = ZIO.fromTry(t)



  // From either
  lazy val e: Either[String, Int] = ???
  lazy val z6: IO[String, Int] = ZIO.fromEither(e)




  // From option
  lazy val opt : Option[Int] = ???
  lazy val z7: IO[Option[Nothing], Int] = ZIO.fromOption(opt)
  lazy val z77: URIO[Any, Option[Int]] = z7.option
  lazy val z78: ZIO[Any, Option[Nothing], Int] = z77.some

  type User
  type Address


  def getUser(): Task[Option[User]] = ???
  def getAddress(u: User): Task[Option[Address]] = ???

  val r = for{
    user <- getUser().some
    address <- getAddress(user)
  } yield (user, address)



  // From function
  lazy val z8: URIO[Int, String] = ZIO.fromFunction[Int, String](i => i.toString)

  // особые версии конструкторов

  lazy val _: UIO[Unit] = ZIO.unit

  lazy val _: UIO[Option[Nothing]] = ZIO.none

  lazy val _: UIO[Nothing] = ZIO.never // while(true)

  lazy val _: ZIO[Any, Nothing, Nothing] = ZIO.die(new Throwable("Ooops"))

  lazy val _: ZIO[Any, Int, Nothing] = ZIO.fail(10)

}



object zioOperators {

  /** *
   *
   * 1. Создать ZIO эффект который будет читать строку из консоли
   */

  lazy val readLine = ???

  /** *
   *
   * 2. Создать ZIO эффект который будет писать строку в консоль
   */

  def writeLine(str: String) = ???

  /** *
   * 3. Создать ZIO эффект котрый будет трансформировать эффект содержащий строку в эффект содержащий Int
   */

  lazy val lineToInt = ???
  /** *
   * 3.Создать ZIO эффект, который будет работать как echo для консоли
   *
   */

  lazy val echo = ???

  /**
   * Создать ZIO эффект, который будет привествовать пользователя и говорить, что он работает как echo
   */

  lazy val greetAndEcho = ???

  // Другие варианты композиции

  lazy val a1: Task[Unit] = ??? // println()
  lazy val b1: Task[String] = ???


  lazy val z9: ZIO[Any, Throwable, (Unit, String)] = a1 zip b1

  lazy val z10: ZIO[Any, Throwable, String] = a1 *> b1

  lazy val z11: ZIO[Any, Throwable, Unit] = a1 <* b1


  // greet and echo улучшенный
  lazy val _: ZIO[Any, Throwable, Unit] = ???


  /**
   * Используя уже созданные эффекты, написать программу, которая будет считывать поочереди считывать две
   * строки из консоли, преобразовывать их в числа, а затем складывать их
   */

  val r1 = ???

  /**
   * Второй вариант
   */

  val r2: ZIO[Any, Throwable, Int] = ???

  /**
   * Доработать написанную программу, чтобы она еще печатала результат вычисления в консоль
   */

  lazy val r3 = ???


  lazy val a: Task[Int] = ???
  lazy val b: Task[String] = ???

  /**
   * последовательная комбинация эффектов a и b
   */
  lazy val ab1: ZIO[Any, Throwable, (Int, String)] = ???

  /**
   * последовательная комбинация эффектов a и b
   */
  lazy val ab2: ZIO[Any, Throwable, Int] = ??? 

  /**
   * последовательная комбинация эффектов a и b
   */
  lazy val ab3: ZIO[Any, Throwable, String] = ??? 


  /**
   * Последовательная комбинация эффета b и b, при этом результатом должна быть конкатенация
   * возвращаемых значений
   */
  lazy val ab4: ZIO[Any,Throwable, String] = b.zipWith(b)(_ + _)


  /**
    * 
    * Другой эффект в случае ошибки
    */

    val ab5 = ???

  /**
    * 
    * A as B
    */


    trait UserValidationError


  def readFile(fileName: String): ZIO[Any, IOException, String] = ???

  // из эффекта с ошибкой, в эффект который не падает

  val d: URIO[Any, String] = readFile("file").orDie
  
}
