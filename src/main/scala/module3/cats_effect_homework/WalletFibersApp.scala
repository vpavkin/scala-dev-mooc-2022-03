package module3.cats_effect_homework

import cats.effect.{IO, IOApp}
import cats.implicits._
import scala.concurrent.duration.DurationLong

// Поиграемся с кошельками на файлах и файберами.

// Нужно написать программу где инициализируются три разных кошелька и для каждого из них работает фоновый процесс,
// который регулярно пополняет кошелек на 100 рублей раз в определенный промежуток времени. Промежуток надо сделать разный, чтобы легче было наблюдать разницу.
// Для определенности: первый кошелек пополняем раз в 100ms, второй каждые 500ms и третий каждые 2000ms.
// Помимо этих трёх фоновых процессов (подсказка - это файберы), нужен четвертый, который раз в одну секунду будет выводить балансы всех трех кошельков в консоль.
// Основной процесс программы должен просто ждать ввода пользователя (IO.readline) и завершить программу (включая все фоновые процессы) когда ввод будет получен.
// Итого у нас 5 процессов: 3 фоновых процесса регулярного пополнения кошельков, 1 фоновый процесс регулярного вывода балансов на экран и 1 основной процесс просто ждущий ввода пользователя.

// Можно делать всё на IO, tagless final тут не нужен.

// Подсказка: чтобы сделать бесконечный цикл на IO достаточно сделать рекурсивный вызов через flatMap:
// def loop(): IO[Unit] = IO.println("hello").flatMap(_ => loop())
object WalletFibersApp extends IOApp.Simple {
  
  def processWallet(wallet: Wallet[IO], amount: BigDecimal, sleepMs: Long): IO[Unit] =
    (wallet.topup(amount) *> IO.sleep(sleepMs.millis))
      .flatMap(_ => processWallet(wallet, amount, sleepMs))

  def printBalance(wallet: Wallet[IO], name: String, sleepMs: Long): IO[Unit] =
    for {
      b <- wallet.balance
      _ <- IO.println(s"$name: $b")
      _ <- IO.sleep(sleepMs.millis)
      _ <- printBalance(wallet, name, sleepMs)
    } yield ()

  def run: IO[Unit] =
    for {
      _ <- IO.println("Press any key to stop...")
      wallet1 <- Wallet.fileWallet[IO]("1")
      wallet2 <- Wallet.fileWallet[IO]("2")
      wallet3 <- Wallet.fileWallet[IO]("3")
      _ <- Spawn[IO].start(processWallet(wallet1, 100, 100))
      _ <- Spawn[IO].start(processWallet(wallet2, 100, 500))
      _ <- Spawn[IO].start(processWallet(wallet3, 100, 2000))
      _ <- Spawn[IO].start(
        printBalance(wallet1, "wallet1", 1000) &>
          printBalance(wallet2, "wallet2",1000) &>
            printBalance(wallet3, "wallet3",1000)
      )
      _ <- IO.readLine.iterateWhile(_.isEmpty)
    } yield ()

}
