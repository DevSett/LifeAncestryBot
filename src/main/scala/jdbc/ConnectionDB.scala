package jdbc


import jdbc.dto.{AncestryTable, PermissionsTable, PhotoAncestryTable, PhotoTreeTable, PhotosTable, TreesTable, User, UsersTable}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ConnectionDB {
  lazy val db = Database.forConfig("sqlite")
  val ancestry = TableQuery[AncestryTable]
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
    ancestry.schema.createIfNotExists,
    permissions.schema.createIfNotExists,
    photoAncestry.schema.createIfNotExists,
    photoTrees.schema.createIfNotExists
  )), Duration.Inf)

  def close = db.close
}
