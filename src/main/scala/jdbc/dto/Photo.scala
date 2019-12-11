package jdbc.dto

import slick.jdbc.H2Profile.api._

final case class Photo(id: Int, bytes: String)

class PhotosTable(tag: Tag) extends Table[Photo](tag, "PHOTO") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def bytes = column[String]("BYTES")

  def * = (id, bytes) <> (Photo.tupled, Photo.unapply)

}