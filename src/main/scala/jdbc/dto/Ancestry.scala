package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class Ancestry(id: Int, firstName: String, secondName: String, middleName: String, description: String,
                          gender: Boolean, primaryPhotoId: Int, father: Int, motherId: Int, treeId: Int)

class AncestryTable(tag: Tag) extends Table[Ancestry](tag, "ANCESTRY") {

  var ancestrys = TableQuery[AncestryTable]
  var trees = TableQuery[TreesTable]
  var photos = TableQuery[PhotosTable]

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("FIRST_NAME")

  def secondName = column[String]("SECOND_NAME")

  def middleName = column[String]("MIDDLE_NAME")

  def description = column[String]("DESCRIPTION")

  def gender = column[Boolean]("GENDER")

  def primaryPhotoId = column[Int]("PRIMARY_PHOTO_ID")

  def fatherId = column[Int]("FATHER_ID")

  def motherId = column[Int]("MOTHER_ID")

  def treeId = column[Int]("TREE_ID")

  def all = (id, firstName, secondName, middleName, description, gender, primaryPhotoId, fatherId, motherId, treeId)

  def * = all <> (Ancestry.tupled, Ancestry.unapply)

  def primaryPhoto =
    foreignKey("PRIMARY_PHOTO", primaryPhotoId, photos)(_.id)

  def mother =
    foreignKey("MOTHER_FK", motherId, ancestrys)(_.id)

  def father =
    foreignKey("FATHER_FK", fatherId, ancestrys)(_.id)

  def tree =
    foreignKey("TREE_FK", treeId, trees)(_.id)

}