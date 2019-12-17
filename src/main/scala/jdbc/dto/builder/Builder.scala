package jdbc.dto.builder

import java.sql.Date

import jdbc.dto.Ancestry

object Builder {
  def buildAncestry(treeId : Int) = Ancestry(0, "", "", "", None,
    false, None, None, None, None, treeId, null)

  def buildAncestryWithFullName(firstName : String, secondName : String, middleName : String, ancestry: Ancestry) =
    Ancestry(0, firstName, secondName, middleName, ancestry.description,
      ancestry.gender, ancestry.primaryPhotoId, ancestry.fatherId, ancestry.motherId, ancestry.partnerId, ancestry.treeId,
      ancestry.birthDate)

  def buildAncestryWithDescription(description : Option[String], ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, description,
      ancestry.gender, ancestry.primaryPhotoId, ancestry.fatherId, ancestry.motherId, ancestry.partnerId, ancestry.treeId,
      ancestry.birthDate)

  def buildAncestryWithGender(gender : Boolean, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      gender, ancestry.primaryPhotoId, ancestry.fatherId, ancestry.motherId, ancestry.partnerId, ancestry.treeId,
      ancestry.birthDate)

  def buildAncestryWithBirthDate(birth : Date, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      ancestry.gender, ancestry.primaryPhotoId, ancestry.fatherId, ancestry.motherId, ancestry.partnerId, ancestry.treeId,
      birth)

  def buildAncestryWithPhoto(photoId : Int, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      ancestry.gender, Some(photoId), ancestry.fatherId, ancestry.motherId, ancestry.partnerId,
      ancestry.treeId, ancestry.birthDate)

  def buildAncestryWithMother(mother : Int, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      ancestry.gender, ancestry.primaryPhotoId, ancestry.fatherId, Some(mother), ancestry.partnerId,
      ancestry.treeId, ancestry.birthDate)

  def buildAncestryWithFather(father : Int, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      ancestry.gender, ancestry.primaryPhotoId, Some(father), ancestry.motherId, ancestry.partnerId,
      ancestry.treeId, ancestry.birthDate)

  def buildAncestryWithPartner(partner : Int, ancestry: Ancestry)=
    Ancestry(0, ancestry.firstName, ancestry.secondName, ancestry.middleName, ancestry.description,
      ancestry.gender, ancestry.primaryPhotoId, ancestry.fatherId, ancestry.motherId, Some(partner),
      ancestry.treeId, ancestry.birthDate)

}
