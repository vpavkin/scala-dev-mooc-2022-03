package module3


import module3.di.UserService
import module3.functional_effects.functionalProgram
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.{ExitCode, URIO, ZIO}

object App {

  def main(args: Array[String]): Unit = {

    def f(v: Clock with  Console with Random): Clock with  Console with Random with UserService = ???
     zio.Runtime.default.unsafeRun(di.e1.provideSome[Clock with  Console with Random](f))

  }
}


object ZioApp extends zio.App{
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    zioConcurrency.printEffectRunningTime(zioConcurrency.p3).exitCode
}