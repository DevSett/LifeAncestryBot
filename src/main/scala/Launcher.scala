import bot.LifeAncestryBot
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import jdbc.ConnectionDB

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Launcher extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    args match {
      case List("LifeAncestryBot", token) =>
        print("start")
        Await.result(new LifeAncestryBot(token).startPolling(),Duration.Inf)
        IO(ExitCode.Success)
      case List(name, _) =>
        IO.raiseError(new Exception(s"Unknown bot $name"))
      case _ =>
        IO.raiseError(new Exception("Usage:\nLauncher $botName $token"))
    }
}