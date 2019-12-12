package bot

import cats.effect.{Async, ContextShift, Timer}
import cats.syntax.functor._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import helper.MessageHelper._
import jdbc.ConnectionDB

class LifeAncestryBot[F[_] : Async : Timer : ContextShift](token: String) extends AbstractBot[F](token)
  with Polling[F]
  with Commands[F]
  with RegexCommands[F] {

  val connectionDB = ConnectionDB

  connectionDB.initTables


  override def receiveMessage(msg: Message): F[Unit] = {
    implicit def message: Message = msg
    implicit def id: Long = msg.source

    if (existTree(userId)) {
      processMsg(commands)
    } else {
      if (processNotAuthMsg(commands)) {
        sendMsg(SUCCESS_AUTH)
      } else {
        sendMsg(CREATE_OR_JOIN)
      }
    }
  }

  def processMsg(commands: Array[String])(implicit id: Long, msg: Message): F[Unit] = commands match {
    case Array("/getAll") => getAll(msg)
    case Array("/add", _) => add(msg)
    case Array("/get", _) => get(msg)
    case Array("/find", _) => find(msg)
    case _ => sendMsg(UNCNOWN_COMMAND)
  }

  def processNotAuthMsg(commands: Array[String])(implicit msg: Message): Boolean = commands match {
    case Array("/create", _) => create(commands)
    case Array("/join", _) => join(commands)
    case _ => false
  }

  def existTree(userId: Int): Boolean = false

  def getAll(msg: Message): F[Unit] = ???

  def get(msg: Message): F[Unit] = ???

  def find(msg: Message): F[Unit] = ???

  def add(msg: Message): F[Unit] = ???

  def join(msg: Array[String]): Boolean = false

  def create(msg: Array[String]) = false

  def sendMsg(msg: String)(implicit id: Long) = request(SendMessage(id, msg)).void

  def userId(implicit message: Message): Int = message.contact.map(_.userId).map(_.getOrElse(-1)).getOrElse(-1)

  def commands(implicit message: Message): Array[String] = message.text.getOrElse("").split("//s+")
}

