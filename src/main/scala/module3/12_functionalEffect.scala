package module3

import scala.io.StdIn

object functional_effects {


  object simpleProgram {

    val greet = {
      println("Как тебя зовут?")
      val name = StdIn.readLine()
      println(s"Привет, $name")
    }

    val askForAge = {
      println("Сколько тебе лет?")
      val age = StdIn.readInt()
      if (age > 18) println("Можешь проходить")
      else println("Ты еще не можешь пройти")
    }


    def greetAndAskForAge = ???


  }


  object functionalProgram {

    /**
     * Executable encoding and Declarative encoding
     */

    object executableEncoding {

      /**
       * 1. Объявить исполняемую модель Console
       */
       case class Console[A](unsafeRun: () => A, run: () => String){ self =>
          def map[B](f: A => B): Console[B] =
            Console.succeed(f(self.unsafeRun()))

          def flatMap[B](f: A => Console[B]): Console[B] =
            Console.succeed(f(self.unsafeRun()).unsafeRun())
       }

      /**
       * 2. Объявить конструкторы
       */

      object Console{
        def succeed[A](a: => A): Console[A] = Console(() => a)
        def printLine(str: String): Console[Unit] = Console(() => println(str))
        def readLine(): Console[String] = Console(() => StdIn.readLine())
      }



      /**
       * 3. Описать желаемую программу с помощью нашей модель
       */


//      val greet = {
//        println("Как тебя зовут?")
//        val name = StdIn.readLine()
//        println(s"Привет, $name")
//      }

      val p1: Console[Unit] = for{
         _ <- Console.printLine("Как тебя зовут?")
         name <- Console.readLine()
         _ <- Console.printLine(s"Привет, $name")
      } yield ()

      val p2: Console[Unit] = for{
        _ <- Console.printLine("Как тебя зовут?")
        name <- Console.readLine()
        _ <- Console.printLine(s"Привет, $name")
      } yield ()

      val p3 = for{
        _ <- p1
        _ <- p2
      } yield ()

      /**
       * 4. Написать операторы
       */

    }


    object declarativeEncoding {

      /**
       * 1. Объявить декларативную модель Console
       */

      sealed trait Console[A]
      case class PrintLine[A](str: String, rest: Console[A]) extends Console[A]
      case class ReadLine[A](f: String => Console[A]) extends Console[A]
      case class Return[A](v: () => A) extends Console[A]

      /**
       * 1. Объявить декларативную модель Console
       */


      /**
       * 2. Написать конструкторы
       * 
       */

      object Console {
        def succeed[A](v: => A): Console[A] = Return(() => v)
        def printLine(string: String): Console[Unit] = PrintLine(string, succeed())
        def readLine: Console[String] = ReadLine(str => succeed(str))
      }


      /**
       * 3. Описать желаемую программу с помощью нашей модели
       */

      //      val greet = {
      //        println("Как тебя зовут?")
      //        val name = StdIn.readLine()
      //        println(s"Привет, $name")
      //      }


      PrintLine("Как тебя зовут?", Console.readLine)

      object consoleOps{

        implicit class ConsoleOps[A](console: Console[A]){
          def map[B](f: A => B): Console[B] = console.flatMap{ a =>
            Console.succeed(f(a))
          }

          def flatMap[B](f: A => Console[B]): Console[B] = console match {
            case PrintLine(str, rest) =>
              PrintLine(str, rest.flatMap(f))
            case ReadLine(ff) => ReadLine(str => ff(str).flatMap(f))
            case Return(v) => f(v())
          }

        }
      }

      import consoleOps._

      val p1: Console[Unit] = for{
        _ <- Console.printLine("Как тебя зовут?")
        name <- Console.readLine
        _ <- Console.printLine(s"Привет, $name")
      } yield ()


      /**
       * 4. Написать операторы
       *
       */


      /**
       * 5. Написать интерпретатор для нашей ф-циональной модели
       *
       */


       def interpret[A](console: Console[A]): A = console match {
         case PrintLine(str, rest) =>
          println(str)
          interpret(rest)
         case ReadLine(f) =>
           interpret(f(StdIn.readLine()))
         case Return(v) => v()
       }



      /**
       * Реализуем туже прошрамму что и в случае с исполняемым подходом
       */

    }

  }

}