package module3

import zio.IO
import zio.ZIO
import zio.Cause
import zio.console._


trait Error extends Product
case object E1 extends Error
case object E2 extends Error

object multipleErrors{
    val z1: IO[E1.type, Int] = ZIO.fail(E1)

    val z2: IO[E2.type, Int] = ZIO.fail(E2)

    val result: IO[Error, (Int, Int)] = z1 zipPar z2

    val app = result.tapCause{
        case Cause.Both(c1, c2) =>
          ZIO.effect(println(c1.failureOption)) *> ZIO.effect(println(c2.failureOption))
    }.orElse(ZIO.effect(println("Effect failed")))
    
}
