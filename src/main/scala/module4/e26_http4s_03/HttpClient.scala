package module4.e26_http4s_03

import CirceJson._
import cats.effect.{IO, Ref}
import org.http4s.{EntityDecoder, Request}
import org.http4s.circe._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import CirceEntityDecoder._

object HttpClient {
  val builder = EmberClientBuilder.default[IO].build

  val request = Request[IO]().withUri(uri"http://localhost:8080/hello/world")

  val result = for {
    client <- builder
    response <- client.run(request)
  } yield response

  val response = result.use { res =>
    res.as[User].flatMap(IO.println)
  }

  val printResponse = result.use(IO.println)

  val stringBodyResponse =
    result.use(res => res.body.compile.to(Array).map(new String(_)))

  val pureRequest =
    Ref.of[IO, List[String]](List.empty).flatMap { (db: Restful.Database[IO]) =>
      val routes = Restful.routes(db)
      routes.run(request).value
    }

}
