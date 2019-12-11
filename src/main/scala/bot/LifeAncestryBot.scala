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

  ConnectionDB.initTables

  override def receiveMessage(msg: Message): F[Unit] = {
    if (existTree(msg.contact.map(_.userId.getOrElse(123)).getOrElse(123))) {
      processMsg(msg)
    } else {
      if (processNotAuthMsg(msg.text.get)) {
        sendMsg(msg.source, SUCCESS_AUTH)
      } else {
        sendMsg(msg.source, CREATE_OR_JOIN)
      }
    }
  }

  def processMsg(msg: Message): F[Unit] = msg.text.get.split("//s+").head match {
    case "/getAll" => getAll(msg)
    case "/add" => add(msg)
    case "/get" => get(msg)
    case "/find" => find(msg)
    case _ => sendMsg(msg.source, UNCNOWN_COMMAND)
  }

  def processNotAuthMsg(msg: String): Boolean = msg.split("//s+").head match {
    case "/create" => create(msg)
    case "/join" => join(msg)
    case _ => false
  }

  def existTree(userId: Int): Boolean = false

  def getAll(msg: Message): F[Unit] = ???

  def get(msg: Message): F[Unit] = ???

  def find(msg: Message): F[Unit] = ???

  def add(msg: Message): F[Unit] = ???

  def join(msg: String) = ???

  def create(msg: String) = false

  def sendMsg(id: Long, msg: String) = request(SendMessage(id, msg)).void

}