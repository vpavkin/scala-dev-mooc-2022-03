package module3

import zio.{Has, Task, ULayer, ZIO, ZLayer}
import zio.clock.{Clock, sleep}
import zio.console._
import zio.duration.durationInt
import zio.macros.accessible
import zio.random._

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.io.StdIn
import scala.language.postfixOps

package object zio_homework {
  
  val currentTime: URIO[Clock, Long] = clock.currentTime(TimeUnit.MILLISECONDS)

  def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A] = for{
    start <- currentTime
    r <- zio
    finish <- currentTime
    _ <- putStrLn(s"Running time: ${(finish - start) / 1000.0}")
  } yield r
  
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в когнсоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */

  lazy val guessProgram: RIO[Random with Console, Unit] = for {
    _ <- ZIO.effect(println("Enter number between 1 and 3"))
    input <- ZIO.effect(StdIn.readInt())
    ref <- zio.random.nextIntBetween(1, 3)
    message <- ZIO.effect(input match {
      case in if !Range(1, 4).contains(in) => "Number is not between 1 and 3"
      case in if in == ref => s"You guessed: $in = $ref"
      case in => s"You didn't guess: $in <> $ref"
    })
    _ <- zio.console.putStr(message)
  } yield ()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   * 
   */

  def doWhile[R, E](effect: ZIO[R, E, Boolean]): ZIO[R, E, Unit] = for {
    is <- effect
    _ <- if (is) ZIO.succeed(true) else doWhile(effect)
  } yield ()

  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */
  
  val defaultConfig: AppConfig = AppConfig("defaultapp", "http://defaultapp.com")

  def loadConfigOrDefault: URIO[Console, Unit] = for {
    config <- load.orElse(ZIO.succeed(defaultConfig))
    _ <- zio.console.putStrLn(config.toString)
  } yield ()

  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  
  lazy val eff: URIO[Clock with Random, Int] = for {
    num <- zio.random.nextIntBetween(0, 10)
    _ <- ZIO.sleep(1 second)
  } yield num

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  
  lazy val effects: Seq[URIO[Clock with Random, Int]] = List.fill(10)(eff)

  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекци "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app: RIO[Console with Clock with Random, Unit] = for {
    numbers <- ZIO.collectAll(effects)
    _ <- ZIO.effect(println(numbers.sum))
  } yield ()
  
  lazy val appWithTime: RIO[Console with Clock with Random, Unit] = printEffectRunningTime(app)

  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp: ZIO[Clock with Random, Throwable, Unit] = for {
    numbers <- ZIO.collectAllPar(effects)
    _ <- ZIO.effect(println(numbers.sum))
  } yield ()


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * молжно было использовать аналогично zio.console.putStrLn например
   */
  
  type Metrics = Has[Metrics.Service]

  object Metrics {

    trait Service {
      def printRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A]
    }

    val live: ULayer[Has[Metrics.Service]] = ZLayer.succeed(
      new Service {
        override def printRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A] = for {
          start <- currentTime
          r <- zio
          finish <- currentTime
          _ <- putStrLn(s"Running time: ${(finish - start) / 1000.0}")
        } yield r
      }
    )

    def printRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Metrics with Console with Clock with R, E, A] =
      ZIO.accessM(_.get.printRunningTime(zio))
  }

   /**
     * 6.
     * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
     *
     * 
     */

  lazy val appWithTimeLogg: ZIO[Metrics with Console with Clock with Random, Throwable, Unit] = Metrics.printRunningTime(app)

  /**
    * 
    * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
    */
  
  lazy val appEnv: ULayer[Has[Metrics.Service]] = Metrics.live

  lazy val runApp: ZIO[Console with Clock with Random, Throwable, Unit] = appWithTimeLogg.provideSomeLayer[Console with Clock with Random](appEnv)
  
}
