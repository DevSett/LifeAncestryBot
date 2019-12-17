package jdbc


import java.util.UUID

import jdbc.ConnectionDB.PermissionQuery.getPerm
import jdbc.dto.{Ancestry, AncestryTable, Permission, PermissionsTable, Photo, PhotoAncestry, PhotoAncestryTable, PhotoTree, PhotoTreeTable, PhotosTable, Tree, TreesTable, User, UsersTable}
import permission.Perm
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

object ConnectionDB {
  lazy val db = Database.forConfig("sqlite")
  val ancestrys = TableQuery[AncestryTable]
  val permissions = TableQuery[PermissionsTable]
  val photos = TableQuery[PhotosTable]
  val photoAncestry = TableQuery[PhotoAncestryTable]
  val photoTrees = TableQuery[PhotoTreeTable]
  val trees = TableQuery[TreesTable]
  val users = TableQuery[UsersTable]

  // create the schema

  def initTables = Await.result(db.run(DBIO.seq(
    users.schema.createIfNotExists,
    trees.schema.createIfNotExists,
    photos.schema.createIfNotExists,
    ancestrys.schema.createIfNotExists,
    permissions.schema.createIfNotExists,
    photoAncestry.schema.createIfNotExists,
    photoTrees.schema.createIfNotExists
  )), Duration.Inf)

  object UserQuery {
    def addUser(userKey: Int) = {
      val insertQuery = users returning users.map(_.id) into ((item, id) => item.copy(id = id))
      val action = insertQuery += User(0, userKey, -1, -1)
      db.run(action)
    }

    def getUser(userKey: Int) = {
      Await.result(db.run(users.filter(_.userKey === userKey).result.headOption), Duration.Inf)
    }

    def updateUpload(uploadTree: Int, uploadAncestry: Int, userKey: Int) {
      val updateQuery =
        sql"update USER set UPLOAD_TREE = ${uploadTree}, UPLOAD_ANCESTRY = ${uploadAncestry} where USER_KEY = ${userKey}".as[String]
      Await.result(db.run(updateQuery), Duration.Inf)
    }


    def clearUpload(userKey: Int) {
      updateUpload(-1, -1, userKey)
    }

  }

  object PermissionQuery {
    def addPerm(userId: Int, treeId: Int, perms: Perm*) = {
      val insertQuery = permissions returning permissions.map(_.id) into ((item, id) => item.copy(id = id))
      val action = insertQuery += Permission(0, userId, treeId, perms.map(_.getRight).mkString(","))
      db.run(action)
    }

    def getPerm(userId: Int) = {
      Await.result(db.run(permissions.filter(_.userId === userId).result), Duration.Inf)
    }

    def getPermFromUserKey(userKey: Int) = {
      getPerm(UserQuery.getUser(userKey).get.id)
    }

  }

  object TreeQuery {

    def addTree(name: String) = {
      val insertQuery = trees returning trees.map(_.id) into ((item, id) => item.copy(id = id))
      val action = insertQuery += Tree(0, name, UUID.randomUUID().toString)
      db.run(action)
    }

    def getTree(id: Int) = {
      Await.result(db.run(trees.filter(_.id === id).result.headOption), Duration.Inf)
    }

    def getTreeFromKey(key: String) = {
      Await.result(db.run(trees.filter(_.key === key).result.headOption), Duration.Inf)
    }

    def getTreeFromName(name: String, user: User) = {
      getPerm(user.id).toList.map(perm => TreeQuery.getTree(perm.treeId))
        .filter(tree => tree.isDefined && tree.get.name == name).head
    }

    def getTreeFromUserKey(userKey: Int) = {
      PermissionQuery.getPermFromUserKey(userKey).filter(perm => !perm.treeId.isNaN).map(perm => TreeQuery.getTree(perm.treeId))
    }

    def getTreeFromUserKeyAndName(userKey: Int, name: String) = {
      getTreeFromUserKey(userKey).filter(tree => tree.get.name == name).head
    }

  }

  object PhotoQuery {
    def addPhotoTree(treeId: Int, bts: String) = {
      val res = Await.result(addPhoto(bts), Duration.Inf)
      val action = photoTrees += PhotoTree(res.id, treeId)
      db.run(action)
    }

    def addPhotoAncestry(ancestryId: Int, bts: String) = {
      val res = Await.result(addPhoto(bts), Duration.Inf)
      val action = photoAncestry += PhotoAncestry(res.id, ancestryId)
      db.run(action)
    }

    def addPhoto(bytes: String) = {
      val insertQuery = photos returning photos.map(_.id) into ((item, id) => item.copy(id = id))
      val action = insertQuery += Photo(0, bytes)
      db.run(action)
    }

    def getPhotoFromTree(treeId: Int) = {
      Await.result(db.run(photoTrees.filter(_.treeId === treeId).flatMap(_.photo).result), Duration.Inf)
    }

    def getPhoto(id: Option[Int]) = id match {
      case None => None
      case Some(id) => Await.result(db.run(photos.filter(_.id === id).result.headOption), Duration.Inf)
    }

    def getPhotoFromAncestry(ancestryId: Int) = {
      Await.result(db.run(photoAncestry.filter(_.ancestryId === ancestryId).flatMap(_.photo).result), Duration.Inf)
    }
  }

  object AncestryQuery {
    def getAll(treeId: Int) = {
      Await.result(db.run(ancestrys.filter(_.treeId === treeId).result), Duration.Inf)
    }

    def addAncestry(ancestry: Ancestry) = {
      val insertQuery =
        ancestrys returning ancestrys.map(_.id) into ((item, id) => item.copy(id = id))

      val action = insertQuery += Ancestry(0, ancestry.firstName,
        ancestry.secondName, ancestry.middleName, ancestry.description, ancestry.gender, ancestry.primaryPhotoId,
        ancestry.fatherId, ancestry.motherId, ancestry.partnerId, ancestry.treeId, ancestry.birthDate)

      db.run(action)
    }

    def getAncestry(id: Option[Int]) = id match {
      case None => None
      case Some(id) => Await.result(db.run(ancestrys.filter(_.id === id).result.headOption), Duration.Inf)
    }

    def findAncestry(firstName: String, secondName: String, middleName: String) = {
      val query = ancestrys.filter(ancestr => ancestr.firstName === firstName && ancestr.secondName === secondName
        && ancestr.middleName === middleName).result
      Await.result(db.run(query), Duration.Inf)
    }


  }

}
