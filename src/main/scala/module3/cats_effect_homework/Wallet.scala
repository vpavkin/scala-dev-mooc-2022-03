package module3.cats_effect_homework

import cats.effect.Sync
import cats.implicits._
import Wallet._

// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]
  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]
  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_]: Sync](id: WalletId) extends Wallet[F] {
  
  def balance: F[BigDecimal] = for {
    path <- Sync[F].delay(java.nio.file.Paths.get(pathStr(id)))
    lines <- Sync[F].delay(java.nio.file.Files.readAllLines(path))
    balance <- Sync[F].delay(BigDecimal(lines.get(0)))
  } yield balance
  
  def topup(amount: BigDecimal): F[Unit] = for {
    path <- Sync[F].delay(java.nio.file.Paths.get(pathStr(id)))
    linesIn <- Sync[F].delay(java.nio.file.Files.readAllLines(path))
    balanceIn <- Sync[F].delay(BigDecimal(linesIn.get(0)))
    balanceOut <- Sync[F].delay((balanceIn + amount).toString())
    _ <- Sync[F].delay(overwriteFile(path, balanceOut))
  } yield()
  
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] = for {
    path <- Sync[F].delay(java.nio.file.Paths.get(pathStr(id)))
    linesIn <- Sync[F].delay(java.nio.file.Files.readAllLines(path))
    balanceIn <- Sync[F].delay(BigDecimal(linesIn.get(0)))
    balanceOut <- Sync[F].delay(subtractBalance(balanceIn, amount))
    res <- Sync[F].delay(balanceOut.map(b => overwriteFile(path, b)))
    _ <- res.liftTo[F]
  } yield res
}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов
  
  def fileWallet[F[_]: Sync](id: WalletId): F[Wallet[F]] = for {
    wallet <- Sync[F].delay(new FileWallet[F](id))
    path <- Sync[F].delay(java.nio.file.Paths.get(pathStr(id)))
    file <- Sync[F].delay(createFile(path))
    _ <- Sync[F].delay(file.fold(_ => (), identity))
  } yield wallet

  type WalletId = String

  sealed trait WalletError extends Throwable
  case object BalanceTooLow extends WalletError
  case object WalletIsExists extends WalletError
  
  def pathStr(id: WalletId): WalletId = "src/main/resources/wallets/" + id

  def overwriteFile(path: Path, content: Any): Unit = {
    java.nio.file.Files.write(path, content.toString.getBytes(), StandardOpenOption.TRUNCATE_EXISTING)
  }

  def subtractBalance(balanceIn: BigDecimal, amount: BigDecimal): Either[WalletError, BigDecimal] =
    Either.cond(balanceIn >= amount, balanceIn - amount, BalanceTooLow)

  def createFile(path: Path): Either[WalletError, Unit] = {
    val exists = java.nio.file.Files.exists(path)
    lazy val startBalance = "0.0"
    Either.cond(
      !exists,
      java.nio.file.Files.write(path, startBalance.getBytes(), StandardOpenOption.CREATE),
      WalletIsExists
    )
  }
}
