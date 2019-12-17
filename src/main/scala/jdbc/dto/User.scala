package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class User(id: Int, userKey: Int, uploadTree : Int, uploadAncestry : Int)

class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def userKey = column[Int]("USER_KEY", O.Unique)

  def uploadTree = column[Int]("UPLOAD_TREE")
  def uploadAncestry = column[Int]("UPLOAD_ANCESTRY")

  def * = (id, userKey, uploadTree, uploadAncestry) <> (User.tupled, User.unapply)
}
