package bot

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Base64

import cats.effect.{Async, ContextShift, Timer}
import cats.syntax.functor._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models._
import helper.MessageHelper._
import jdbc.ConnectionDB

import scala.concurrent.{Await, Future}
import com.bot4s.telegram.Implicits._
import cats.instances.future._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.clients.AkkaHttpClient
import com.bot4s.telegram.api.AkkaTelegramBot
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import jdbc.ConnectionDB.{AncestryQuery, PermissionQuery, PhotoQuery, TreeQuery, UserQuery}
import jdbc.dto.Ancestry
import jdbc.dto.builder.Builder._
import permission.{AddState, Perm}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


class LifeAncestryBot(token: String) extends AkkaTelegramBot
  with Polling[Future]
  with Commands[Future] {

  val addables = mutable.HashMap.empty[Int, permission.AddState.Value]
  val addablesAncestry = mutable.HashMap.empty[Int, Ancestry]

  val connectionDB = ConnectionDB
  connectionDB.initTables

  override val client = new AkkaHttpClient(token)


  implicit def id(implicit message: Message): Long = message.source

  implicit def commands(implicit message: Message): Array[String] = message.text.getOrElse("").split(" ")

  def userKey(implicit message: Message): Int = message.from.map(_.id).get

  def user(implicit message: Message) = UserQuery.getUser(userKey)

  def tree(implicit message: Message) = TreeQuery.getTreeFromUserKey(userKey).head

  def sendMsg(msg: String)(implicit id: Long) = request(SendMessage(id, msg)).void

  override def receiveMessage(msg: Message): Future[Unit] = {
    implicit val message = msg

    checkAndCreateUser

    if (checkAddables) {
      return unit
    }

    if (existTree) {
      if (isUploading) {
        processUpload
      } else {
        processMsg
      }
      unit
    } else {
      if (processNotAuthMsg) {
        sendMsg(SUCCESS_AUTH)
      } else {
        sendMsg(CREATE_OR_JOIN)
      }
    }
  }


  def processMsg(implicit id: Long, msg: Message, commands: Array[String]) = commands match {
    case Array("/getall") => getAll
    case Array("/add") => add
    case Array("/get", secondName, firstName, middleName, number) => get(firstName, secondName, middleName, number)
    case Array("/find", secondName) => find(secondName)
    case Array("/getphoto", secondName, firstName, middleName, number) => getPhoto(firstName, secondName, middleName, number)
    case Array("/getphototree") => getPhotoTree

    case Array("/getkey") => getKey
    case Array("/uploadtree") => uploadTree
    case Array("/uploadancestry", _, _, _) => uploadAncestry

    case _ => sendMsg(UNKNOWN_COMMAND)
  }

  //command find
  def find(secondName: String)(implicit msg: Message): Future[Unit] = {
    val ancestrySeq = AncestryQuery.findAncestryFromSecondName(secondName)
    if (ancestrySeq.isEmpty) {
      sendMsg(NOT_FOUND_ANCESTRY_NAME)
      return unit
    }
    var index = 0
    sendMsg( ancestrySeq.zipWithIndex.map{case (anc, index) => s"${index+1}: ${fullName(anc)}"}.mkString("\n"))
  }

  //command getAll
  def getAll(implicit msg: Message): Future[Unit] = {
    val ancestrySeq = AncestryQuery.getAll(tree.get.id)
    if (ancestrySeq.isEmpty) {
      sendMsg(EMPTY_ANCESTRY)
      return unit
    }

    sendMsg( ancestrySeq.zipWithIndex.map{case (anc, index) => s"${index+1}: ${fullName(anc)}"}.mkString("\n"))
  }

  //command add
  def add(implicit msg: Message): Future[Unit] = {
    addables.put(userKey, AddState.Name)
    addablesAncestry.put(userKey, buildAncestry(tree.get.id))
    sendMsg(ADD_NAME)
    unit
  }

  //command join
  def join(key: String)(implicit msg: Message): Boolean = {
    val tree = TreeQuery.getTreeFromKey(key)
    if (tree.isEmpty) {
      sendMsg(NOT_FOUND_TREE_KEY)
      return false
    }
    PermissionQuery.addPerm(user.get.id, tree.get.id, Perm.ADD, Perm.EDIT, Perm.READ)
    true
  }

  //command create
  def create(name: String)(implicit msg: Message): Boolean = {
    val tree = Await.result(TreeQuery.addTree(name), Duration.Inf)
    PermissionQuery.addPerm(user.get.id, tree.id, Perm.ADD, Perm.EDIT, Perm.READ)
    return true
  }

  //command getPhotoTree
  def getPhotoTree(implicit msg: Message): Future[Unit] = {
    val treeTemp = tree
    PhotoQuery.getPhotoFromTree(treeTemp.get.id)
      .map(photo => InputFile("", decodeToByteArray(photo.bytes)))
      .foreach(file => request(SendPhoto(id, file)))
    unit
  }


  //command getPhoto
  def getPhoto(firstName: String, secondName: String, middleName: String, number: String)(implicit msg: Message): Future[Unit] =
    getAncestry(firstName, secondName, middleName, number) match {
      case None => unit
      case Some(ancestry) =>
        PhotoQuery.getPhotoFromAncestry(ancestry.get.id)
          .map(photo => InputFile("", decodeToByteArray(photo.bytes)))
          .foreach(file => request(SendPhoto(id, file)))
        unit
    }

  //command getKey
  def getKey(implicit msg: Message): Future[Unit] = {
    sendMsg(tree.get.key)
  }

  //command get
  def get(firstName: String, secondName: String, middleName: String, number: String)(implicit msg: Message): Future[Unit] =
    getAncestry(firstName, secondName, middleName, number) match {
      case None => unit
      case Some(ancestry) =>
        sendMsg(outAncestry(ancestry, AncestryQuery.getAncestry(ancestry.fatherId),
          AncestryQuery.getAncestry(ancestry.motherId), AncestryQuery.getAncestry(ancestry.partnerId)))
        PhotoQuery.getPhoto(ancestry.primaryPhotoId) match {
          case None => unit
          case Some(photo) => request(SendPhoto(id, InputFile("", decodeToByteArray(photo.bytes)))).void
        }

    }


  def getAncestry(firstName: String, secondName: String, middleName: String, number: String)(implicit msg: Message): Option[Ancestry] = {
    val ancestrySeq = AncestryQuery.findAncestry(firstName, secondName, middleName)
    if (ancestrySeq.isEmpty) {
      sendMsg(NOT_FOUND_ANCESTRY_NAME)
      return None
    }
    try {
      number.toInt
    } catch {
      case e: java.lang.NumberFormatException => sendMsg(UNKNOWN_NUMBER)
        return None
    }

    var calcNumber = 0
    if (ancestrySeq.size > number.toInt) {
      calcNumber = number.toInt
    }

    ancestrySeq(calcNumber)
  }

  def checkAddables(implicit message: Message, commands: Array[String]): Boolean = addables.get(userKey) match {
    case None => false
    case Some(state) => checkStateAddable(state)
  }

  def uploadAncestry(implicit msg: Message, commands: Array[String]): Future[Unit] = {
    val ancestrys =
      AncestryQuery.findAncestry(commands(2), commands(1), commands(3))
    if (ancestrys.isEmpty) {
      return sendMsg(NOT_FOUND_ANCESTRY_NAME)
    }
    if (ancestrys.size > 1) {
      return sendMsg(VERY_MORE_ANCESTRY)
    }
    val ancestry = ancestrys.get.head
    UserQuery.updateUpload(-1, ancestry.id, userKey)
    sendMsg(START_UPLOAD)
  }

  def uploadTree(implicit msg: Message, commands: Array[String]): Future[Unit] = {
    val tree = TreeQuery.getTreeFromUserKey(userKey).head
    if (tree.isEmpty) {
      return sendMsg(NOT_FOUND_TREE)
    }
    UserQuery.updateUpload(tree.get.id, -1, userKey)
    sendMsg(START_UPLOAD)
  }

  def stopUpload(implicit msg: Message) {
    UserQuery.clearUpload(userKey)
  }


  def processNotAuthMsg(implicit msg: Message, commands: Array[String]): Boolean = commands match {
    case Array("/create", name) => create(name)
    case Array("/join", key) => join(key)
    case _ => false
  }

  def existTree(implicit msg: Message): Boolean = {
    PermissionQuery.getPermFromUserKey(userKey).nonEmpty
  }

  def checkAndCreateUser(implicit msg: Message) {
    val userTemp = user
    if (userTemp.isEmpty) {
      Await.result(UserQuery.addUser(userKey), Duration.Inf)
    }
  }

  def decodeToByteArray(bytes: String) = Base64.getDecoder.decode(bytes)


  def isAddableStateSkip(allowSkip: Boolean)(implicit msg: Message, commands: Array[String]): Boolean = commands match {
    case Array("/skip") =>
      if (!allowSkip) {
        true
      } else {
        sendMsg(NOT_ALLOWED_SKIP)
        false
      }
    case _ => false
  }

  def isUploading(implicit msg: Message) = user match {
    case Some(user) => user.uploadTree != -1 || user.uploadAncestry != -1
    case None => false
  }

  def processUpload(implicit msg: Message, commands: Array[String]) = commands match {
    case Array("/stop") => stopUpload
    case _ => msg.photo match {
      case None => sendMsg(UNKNOWN_COMMAND)
      case Some(photo) => checkAndSaveFile(true)
    }
  }

  def checkAndSaveFile(isBasic: Boolean)(implicit msg: Message): Int = {
    var photoId = -1
    Await.result(request(GetFile(msg.photo.get.last.fileId)).andThen({
      case Success(file) => {
        file.filePath match {
          case Some(filePath) => {
            val url = s"https://api.telegram.org/file/bot${token}/${filePath}"
            val bytes = Await.result(for {
              res <- Http().singleRequest(HttpRequest(uri = Uri(url)))
              if res.status.isSuccess()
              bytes <- Unmarshal(res).to[ByteString]
            } yield (bytes), Duration.Inf)

            val base64 = Base64.getEncoder.encodeToString(bytes.toArray)

            if (isBasic) {
              val userT = user.get
              if (userT.uploadTree != -1) {
                PhotoQuery.addPhotoTree(userT.uploadTree, base64)
              }
              if (userT.uploadAncestry != -1) {
                PhotoQuery.addPhotoAncestry(userT.uploadAncestry, base64)
              }
            } else {
              photoId = Await.result(PhotoQuery.addPhoto(base64), Duration.Inf).id
            }
          }
          case None =>
            reply("No file_path was returned")
        }
      }
    }), Duration.Inf)
    photoId
  }

  def checkStateAddable(state: AddState.Value)(implicit message: Message, commands: Array[String]): Boolean = state match {
    case AddState.Name => {
      commands match {
        case Array("/skip") => {
          sendMsg(NOT_ALLOWED_SKIP)
          return true
        }
        case Array(secondName, firstName, middleName) => {
          addablesAncestry.put(userKey, buildAncestryWithFullName(firstName, secondName, middleName, addablesAncestry(userKey)))
          addables.put(userKey, AddState.Description)
          sendMsg(ADD_DESCRIPTION)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case _ =>
          sendMsg(NEED_FULL_NAME)
      }
      true
    }
    case AddState.Description => {
      commands match {
        case Array("/skip") => {
          addables.put(userKey, AddState.Gender)
          sendMsg(ADD_GENDER)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case _ => {
          addablesAncestry.put(userKey, buildAncestryWithDescription(message.text, addablesAncestry(userKey)))
          addables.put(userKey, AddState.Gender)
          sendMsg(ADD_GENDER)
        }
      }
      true
    }
    case AddState.Gender => {
      commands match {
        case Array("/skip") => {
          sendMsg(NOT_ALLOWED_SKIP)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array("м") => {
          addablesAncestry.put(userKey, buildAncestryWithGender(gender = true, addablesAncestry(userKey)))
          addables.put(userKey, AddState.BirthDate)
          sendMsg(ADD_BIRTH_DATE)
          return true
        }
        case Array("ж") => {
          addablesAncestry.put(userKey, buildAncestryWithGender(gender = false, addablesAncestry(userKey)))
          addables.put(userKey, AddState.BirthDate)
          sendMsg(ADD_BIRTH_DATE)
          return true
        }
        case Array(_) => return true
      }
      true
    }

    case AddState.BirthDate
    => {
      commands match {
        case Array("/skip") => {
          sendMsg(NOT_ALLOWED_SKIP)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array(dateString) => {
          try {
            val date = new Date(new SimpleDateFormat("dd.MM.yyyy").parse(dateString).getTime)
            addablesAncestry.put(userKey, buildAncestryWithBirthDate(date, addablesAncestry(userKey)))
            addables.put(userKey, AddState.Photo)
            sendMsg(ADD_PHOTO)
          } catch {
            case e: java.text.ParseException => sendMsg(NOT_CORRECT_DATE)
          }
          return true
        }

      }
      true
    }
    case AddState.Photo
    => {
      commands match {
        case Array("/skip") => {
          addables.put(userKey, AddState.Father)
          sendMsg(ADD_FATHER)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array("") => {
          if (message.photo.isEmpty) {
            return true
          }
          val idFile = checkAndSaveFile(isBasic = false)
          addablesAncestry.put(userKey, buildAncestryWithPhoto(idFile, addablesAncestry(userKey)))
          addables.put(userKey, AddState.Father)
          sendMsg(ADD_FATHER)
        }
      }
      true
    }
    case AddState.Father
    => {
      commands match {
        case Array("/skip") => {
          addables.put(userKey, AddState.Mother)
          sendMsg(ADD_MOTHER)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array(secondName, firstName, middleName) => {
          val ancestry = AncestryQuery.findAncestry(firstName, secondName, middleName).head
          if (ancestry.isEmpty) {
            sendMsg(NOT_FOUND_ANCESTRY_NAME)
            return true
          }

          addablesAncestry.put(userKey, buildAncestryWithFather(ancestry.id, addablesAncestry(userKey)))
          addables.put(userKey, AddState.Mother)
          sendMsg(ADD_MOTHER)
          return true
        }
        case _ =>
          sendMsg(NEED_FULL_NAME)
      }
      true
    }
    case AddState.Mother
    => {
      commands match {
        case Array("/skip") => {
          addables.put(userKey, AddState.Partner)
          sendMsg(ADD_PARTNER)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array(secondName, firstName, middleName) => {
          val ancestry = AncestryQuery.findAncestry(firstName, secondName, middleName).head
          if (ancestry.isEmpty) {
            sendMsg(NOT_FOUND_ANCESTRY_NAME)
            return true
          }

          addablesAncestry.put(userKey, buildAncestryWithMother(ancestry.id, addablesAncestry(userKey)))
          addables.put(userKey, AddState.Partner)
          sendMsg(ADD_PARTNER)
          return true
        }
        case _ =>
          sendMsg(NEED_FULL_NAME)
      }
      true
    }
    case AddState.Partner
    => {
      commands match {
        case Array("/skip") => {
          AncestryQuery.addAncestry(addablesAncestry(userKey))
          addablesAncestry.remove(userKey)
          addables.remove(userKey)
          sendMsg(SUCCESS_ADD)
          return true
        }
        case Array("/stop") => {
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          return true
        }
        case Array(secondName, firstName, middleName) => {
          val ancestry = AncestryQuery.findAncestry(firstName, secondName, middleName).head
          if (ancestry.isEmpty) {
            sendMsg(NOT_FOUND_ANCESTRY_NAME)
            return true
          }

          val ancestryRes = buildAncestryWithPartner(ancestry.id, addablesAncestry(userKey))
          AncestryQuery.addAncestry(ancestryRes)
          addables.remove(userKey)
          addablesAncestry.remove(userKey)
          sendMsg(SUCCESS_ADD)
          return true
        }
        case _ =>
          sendMsg(NEED_FULL_NAME)
      }
      true
    }
  }

}

