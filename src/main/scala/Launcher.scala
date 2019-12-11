import bot.LifeAncestryBot
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import jdbc.ConnectionDB

object Launcher extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    args match {
      case List("LifeAncestryBot", token) =>
        new LifeAncestryBot[IO](token).startPolling.map(_ => ExitCode.Success)
      case List(name, _) =>
        IO.raiseError(new Exception(s"Unknown bot $name"))
      case _ =>
        IO.raiseError(new Exception("Usage:\nLauncher $botName $token"))
    }
}