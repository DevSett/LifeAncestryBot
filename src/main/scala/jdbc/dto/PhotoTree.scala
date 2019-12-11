package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class PhotoTree(photoId: Int, treeId: Int)

class PhotoTreeTable(tag: Tag) extends Table[PhotoTree](tag, "PHOTOTREE") {
  var trees = TableQuery[TreesTable]
  var photos = TableQuery[PhotosTable]

  def photoId = column[Int]("PHOTO_ID")

  def treeId = column[Int]("TREE_ID")

  def * = (photoId, treeId) <> (PhotoTree.tupled, PhotoTree.unapply)

  def photo =
    foreignKey("PHOTO_FK", photoId, photos)(_.id)

  def tree =
    foreignKey("TREE_FK", treeId, trees)(_.id)

}