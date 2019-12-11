package jdbc.dto

final case class PhotoAncestry(photoId: Int, ancestryId: Int)

import slick.jdbc.H2Profile.api._

class PhotoAncestryTable(tag: Tag) extends Table[PhotoAncestry](tag, "PHOTOANCESTRY") {
  var ancestrys = TableQuery[AncestryTable]
  var photos = TableQuery[PhotosTable]

  def photoId = column[Int]("PHOTO_ID")

  def ancestryId = column[Int]("ANCESTRY_ID")

  def * = (photoId, ancestryId) <> (PhotoAncestry.tupled, PhotoAncestry.unapply)

  def photo =
    foreignKey("PHOTO_FK", photoId, photos)(_.id)

  def ancestry =
    foreignKey("ANCESTRY_FK", ancestryId, ancestrys)(_.id)

}