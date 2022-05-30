package module2
import cats.Id
import cats.data.{Chain, Ior, Kleisli, NonEmptyChain, NonEmptyList, OptionT, Validated, Writer, WriterT}
import cats.implicits._

import scala.concurrent.Future
import scala.util.Try

object dataStructures{

  /**
   * Chain
   */

  // Конструкторы

  val empty: Chain[Int] = Chain()
  val empty2: Chain[Int] = Chain.empty[Int]

  val ch2: Chain[Int] = Chain(1)
  val ch3: Chain[Int] = Chain.one(1)
  val ch4: Chain[Int] = Chain.fromSeq(List(1, 2, 3))


  // операторы

  val ch5 = ch2 :+ 2
  val ch6 = 3 +: ch2 :+ 2
  val r = ch2.headOption

  /**
   * NonEmptyChain
   */

  // конструкторы

  val nec: NonEmptyChain[Int] = NonEmptyChain(1)
  val nec2: NonEmptyChain[Int] = NonEmptyChain.one(1)
  val nec3: Option[NonEmptyChain[Int]] = NonEmptyChain.fromSeq(List(1, 2, 3))

  val r2 =  nec2.head

  /**
   * NonEmptyList
   **/

  val nel1: NonEmptyList[Int] = NonEmptyList(1, List(2, 3))
  val nel2: NonEmptyList[Int] = NonEmptyList.one(1)
  val nel3: Option[NonEmptyList[Int]] = List(1, 2, 3).toNel

}

object validation{

  type EmailValidationError = String
  type NameValidationError = String
  type AgeValidationError = String
  type Name = String
  type Email = String
  type Age = Int

  case class UserDTO(email: String, name: String, age: Int)
  case class User(email: String, name: String, age: Int)

  def emailValidatorE: Either[EmailValidationError, Email] =  Right("foo@mail.com")

  def userNameValidatorE: Either[NameValidationError, Name] = Left("Name invalid")

  def userAgeValidatorE: Either[AgeValidationError, Age] = Left("Age invalid")


  // короткое замыкание
  def validateUserDataE(userDTO: UserDTO): Either[String, User] = for{
    email <- emailValidatorE
    name <- userNameValidatorE
    age <- userAgeValidatorE
  } yield User(email, name, age)

  // Validated

  val v1: Validated[String, String] = Validated.valid[String, String]("foo@mail.com")
  val v2: Validated[String, String] = Validated.invalid[String, String]("Email invalid")


  //Конструкторы


  def emailValidatorV: Validated[String, Email] = "Email invalid".invalid[Email]
  def userNameValidatorV: Validated[String, Name] = "Name invalid".invalid[Name]
  def userAgeValidatorV: Validated[AgeValidationError, Age] = 30.valid[AgeValidationError]

  // Операторы

  emailValidatorV.map(_ + "frgvrt")
  emailValidatorV.bimap(e => e + "ooops", v => v + "_yeah")
  emailValidatorV combine userNameValidatorV

  // Решаем задачу валидации с помощью Validated


  // не компилируется
  // Validated НЕ МОНАДА
//  def validateUserDataV(userDTO: UserDTO): Validated[String, User] = for{
//    email <- emailValidatorV
//    name <- userNameValidatorV
//    age <- userAgeValidatorV
//  } yield User(email, name, age)

  def validateUserDataV2(userDTO: UserDTO): Validated[String, String] =
    emailValidatorV combine userNameValidatorV combine userAgeValidatorV.map(_.toString)

  def validateUserDataV3(userDTO: UserDTO): Validated[NonEmptyChain[String], String] =
    emailValidatorV.toValidatedNec combine userNameValidatorV.toValidatedNec combine
        userAgeValidatorV.map(_.toString).toValidatedNec

  def validateUserDataV4(userDTO: UserDTO) = (
    emailValidatorV.toValidatedNec, userNameValidatorV.toValidatedNec,
      userAgeValidatorV.toValidatedNec
  ).mapN{ (email, name, age) =>
    User(email, name, age)
  }

   // Ior

  val u: User = User("a", "b", 30)

  // Конструкторы

  val ior: Ior[String, User] = Ior.Left("Error")
  val ior2: Ior[String, User] = Ior.Right(u)
  val ior3 = Ior.Both("Warning", u)

  // Операторы


  // Решаем задачу

  def emailValidatorI = Ior.both("Email warning", "f@foo.com")
  def userNameValidatorI = Ior.both("Name warning", "John")
  def userAgeValidatorI = 30.rightIor[AgeValidationError]


  // В отличии от Validated является монадой
  def validateUserDataI(userDTO: UserDTO) = for{
    email <- emailValidatorI.toIorNec
    name <- userNameValidatorI.toIorNec
    age <- userAgeValidatorI.toIorNec
  } yield User(email, name, age)

  lazy val validateUserDataI2 = ???
}

object functional {


  // Kleisli

  val f1: Int => String = i => i.toString
  val f2: String => String = s => s + "_oops"


  val f3: Int => String = f1 andThen f2

  val f4: String => Option[Int] = _.toIntOption

  val f5: Int => Option[Int] = i => Try(10 / i).toOption

  val f6: Kleisli[Option, String, Int] = Kleisli(f4) andThen Kleisli(f5)

  // Writer

  val w = Writer("ddd", 100)

}

object transformers {

  val f1: Future[Int] = Future.successful(2)
  def f2(i: Int): Future[Option[Int]] = Future.successful(Try(10 / i).toOption)
  def f3(i: Int): Future[Option[Int]] = Future.successful(Try(10 / i).toOption)

  import scala.concurrent.ExecutionContext.Implicits.global

  val r: OptionT[Future, Int] = for{
    i <- OptionT.liftF(f1)
    v <- OptionT(f2(i))
    v2 <- OptionT(f3(v))
  } yield v + v2

}

object otherDS{

  def id[A](a: A): A = a

  val i1: Id[String] = "Hello"
  val i2: Id[String] = "Foo"

  for{
    v1 <- i1
    v2 <- i2
  } yield v1 + v2

}

object cats_type_classes{

  // Semigroup

  trait Semigroup[A]{
    def combine(x: A, y: A): A
    // combine(x, combine(y, z)) == combine(combine(x, y), z)
  }

  object Semigroup{
    def apply[A](implicit ev: Semigroup[A]) = ev

    implicit val intSemigroup = new Semigroup[Int] {
      override def combine(x: Int, y: Int): Int = x + y
    }
  }

  val s1 = Semigroup[Int].combine(2, 5)

  val l = List(1, 2, 3).foldLeft(0)(_ |+| _)

  // Map("a" -> 1, "b" -> 2)
  // Map("b" -> 3, "c" -> 4)
  // Map("a" -> 1, "b" -> 5, "c" -> 4)

  def optCombine[V: Semigroup](v: V, opt: Option[V]): V =
    opt.map(Semigroup[V].combine(v, _)).getOrElse(v)

  def mergeMap[K, V: Semigroup](l: Map[K, V], r: Map[K, V]): Map[K, V] =
    l.foldLeft(r){ case (acc, (k, v)) =>
      acc.updated(k, optCombine(v, acc.get(k)))
  }

//  def combineAll[A: Semigroup](l: List[A]): A =
//    l.foldLeft(???)(_ |+| _)


  // Monoid

  trait Monoid[A] extends Semigroup[A]{
    def empty: A
  }

  object Monoid{
    def apply[A](implicit ev: Monoid[A]) = ev

    // associativity
    // identity
    // combine(x, combine(y, z)) == combine(combine(x, y), z)
    // combine(x, empty) == x
    // combine(empty, x) == x

  }

  def combineAll[A: Monoid](l: List[A]): A =
      l.foldLeft(Monoid[A].empty)(Monoid[A].combine(_, _))

  // Functor

  trait Functor[F[_]]{
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  def id[A](e: A): A = e

  // Option(1).map(id) == Option(1)
  // map id
  // map function comp
  // map(f andThen g) == map(f).map(g)

}