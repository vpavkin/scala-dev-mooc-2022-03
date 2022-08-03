package module4.e26_http4s_03

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.{Functor, Monad}
import com.comcast.ip4s.{Host, Port}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, Header, HttpRoutes, Request, Status}
import org.typelevel.ci.CIString

object Restful {

  type Database[F[_]] = Ref[F, List[String]]

  def service(db: Database[IO]): HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello" / name =>
        db.get.flatMap(users =>
          if (users.contains(name)) Ok(s"hello, $name")
          else Forbidden("You shall not pass")
        )

      case PUT -> Root / "register" / name =>
        db.update(list => name :: list)
          .flatMap(_ => Ok("User has been registered"))
    }

  def routes(db: Database[IO]) =
    AuthMiddleware(authUser(db)).apply(authRoutes(db))
  def router(db: Database[IO]) =
    Router(
      "/" -> routes(db)
    )

  case class User(name: String)

  def authUser(db: Database[IO]): Kleisli[OptionT[IO, *], Request[IO], User] =
    Kleisli { (req: Request[IO]) =>
      req.headers.get(CIString("username")) match {
        case Some(nel) =>
          OptionT.liftF(db.get).map { users =>
            val username = nel.head.value
            if (users.contains(username)) User(username)
            else User("unregistered")
          }
        case None =>
          OptionT.pure(User("anon"))
      }
    }

  def authRoutes(db: Database[IO]): AuthedRoutes[User, IO] =
    AuthedRoutes.of {
      case GET -> Root / "hello" / path as user =>
        if (user.name != "anon") Ok(s"hello, ${user.name} from $path")
        else Forbidden("You're anonymous")
      case PUT -> Root / "register" / name as _ =>
        db.update(list => name :: list).flatMap { _ =>
          Ok("User has been registered")
        }
    }

  val server = for {
    db <- Resource.eval(Ref.of[IO, List[String]](List.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(router(db).orNotFound)
      .build
  } yield s

  // curl localhost:8080/hello/vladimir --verbose
  // curl -XPUT localhost:8080/register/vladimir --verbose
  // curl -H "username: Vladimir" localhost:8080/hello/otus
}
