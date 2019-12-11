package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class Tree(id : Int, name : String, key : String)

class TreesTable(tag: Tag) extends Table[Tree](tag, "TREE") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def key = column[String]("KEY", O.Unique)

  def * = (id, name, key) <> (Tree.tupled, Tree.unapply)

}