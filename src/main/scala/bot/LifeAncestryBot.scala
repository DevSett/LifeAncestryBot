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
    implicit def commands(implicit message: Message): Array[String] = message.text.getOrElse("").split("//s+")
    def userId(implicit message: Message): Int = message.contact.map(_.userId).map(_.getOrElse(-1)).getOrElse(-1)

    if (existTree(userId)) {
      processMsg
    } else {
      if (processNotAuthMsg) {
        sendMsg(SUCCESS_AUTH)
      } else {
        sendMsg(CREATE_OR_JOIN)
      }
    }
  }

  def processMsg(implicit id: Long, msg: Message, commands: Array[String]): F[Unit] = commands match {
    case Array("/getAll") => getAll
    case Array("/add", _) => add
    case Array("/get", _) => get
    case Array("/find", _) => find
    case _ => sendMsg(UNCNOWN_COMMAND)
  }

  def processNotAuthMsg(implicit msg: Message, commands: Array[String]): Boolean = commands match {
    case Array("/create", _) => create
    case Array("/join", _) => join
    case _ => false
  }

  def existTree(userId: Int): Boolean = ???

  def getAll(implicit msg: Message): F[Unit] = ???

  def get(implicit msg: Message, commands: Array[String]): F[Unit] = ???

  def find(implicit msg: Message, commands: Array[String]): F[Unit] = ???

  def add(implicit msg: Message, commands: Array[String]): F[Unit] = ???

  def join(implicit commands: Array[String]): Boolean = ???

  def create(implicit commands: Array[String]) = ???

  def sendMsg(msg: String)(implicit id: Long) = request(SendMessage(id, msg)).void


}

