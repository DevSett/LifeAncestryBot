package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class User(id: Int, userKey: String)

class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def userKey = column[String]("USER_KEY", O.Unique)

  def * = (id, userKey) <> (User.tupled, User.unapply)
}
