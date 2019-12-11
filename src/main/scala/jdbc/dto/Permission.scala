package jdbc.dto

import slick.jdbc.H2Profile.api._
import slick.lifted.ForeignKeyQuery

final case class Permission(id: Int, userId: Int, treeId: Int, rights: String)

class PermissionsTable(tag: Tag) extends Table[Permission](tag, "PERMISSION") {

  val users = TableQuery[UsersTable]
  var trees = TableQuery[TreesTable]

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def userId = column[Int]("USER_ID")

  def treeId = column[Int]("TREE_ID")

  def rights = column[String]("RIGHTS")

  def * = (id, userId, treeId, rights) <> (Permission.tupled, Permission.unapply)

  def user =
    foreignKey("USER_FK", userId, users)(_.id)

  def tree =
    foreignKey("TREE_FK", treeId, trees)(_.id)

}