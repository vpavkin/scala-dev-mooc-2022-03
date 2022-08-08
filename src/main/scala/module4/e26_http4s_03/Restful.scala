package module4.e26_http4s_03

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.{Functor, Monad}
import com.comcast.ip4s.{Host, Port}
import module4.e26_http4s_03.CirceJson._
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, Header, HttpRoutes, Request, Status}
import org.typelevel.ci.CIString
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
object Restful {

  type Database[F[_]] = Ref[F, List[String]]
//
//  def publicRoutes: HttpRoutes[IO] =
//    HttpRoutes.of { case r @ POST -> Root / "echo" =>
//      for {
//        u <- r.as[User]
//        _ <- IO.println(u)
//        response <- Ok(u)
//      } yield response
//    }

  def routes(db: Database[IO]) =
    AuthMiddleware(authUser(db)).apply(authRoutes(db))
  def router(db: Database[IO]) =
    Router(
      "/" -> routes(db)
//      "/public" -> publicRoutes
    )

  case class AuthUser(name: String)

  def authUser(
      db: Database[IO]
  ): Kleisli[OptionT[IO, *], Request[IO], AuthUser] =
    Kleisli { (req: Request[IO]) =>
      req.headers.get(CIString("username")) match {
        case Some(nel) =>
          OptionT.liftF(db.get).map { users =>
            val username = nel.head.value
            if (users.contains(username)) AuthUser(username)
            else AuthUser("unregistered")
          }
        case None =>
          OptionT.pure(AuthUser("anon"))
      }
    }

  def authRoutes(db: Database[IO]): AuthedRoutes[AuthUser, IO] =
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
