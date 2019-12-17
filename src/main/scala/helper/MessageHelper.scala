package helper

import java.sql.Date
import java.text.SimpleDateFormat

import jdbc.ConnectionDB.AncestryQuery
import jdbc.dto.Ancestry

object MessageHelper {

  val CREATE_OR_JOIN: String = "У вас отсутствует авторизация в древе. Ввидите команду /create <name> или /join <token>."
  val SUCCESS_AUTH: String = "Успешная авторизация."
  val UNKNOWN_COMMAND: String = "Неизвестная команда."
  val NOT_FOUND_TREE: String = "Не найдено древо с переданным названием. Используйте команду /upload НАИМЕНОВАНИЕ_ДРЕВА."
  val NOT_FOUND_ANCESTRY: String = "Не найдено древо с переданным названием. Используйте команду /upload НАИМЕНОВАНИЕ_ДРЕВА."
  val EMPTY_ANCESTRY : String = "Не одного древа не найдено."
  val NOT_FOUND_ANCESTRY_NAME: String = "Не найдено древо с переданным ФИО. Пример: Иванов Иван Иванович."
  val VERY_MORE_ANCESTRY: String = "Найдено больше одной записи, на данный момент данная функция не поддерживается."
  val START_UPLOAD: String = "Можете загружать изображения! Изображение ввиде файлов не будут загружены! Для окончания загрузки введите команду /stop."
  val ERROR: String = "Разработчик где-то накасячил, произошла ошибка."
  val NEED_FULL_NAME: String = "Необходимо ввести ФИО. Для отмены добавления команда /stop"
  val ADD_NAME: String = "Введите ФИО."
  val ADD_GENDER: String = "Введите Пол. ('м'/'ж')"
  val ADD_BIRTH_DATE: String = "Введите дату рождения. Пример: 30.11.1996"
  lazy val ADD_DESCRIPTION: String = "Введите Описание. " + SKIP
  lazy val ADD_PHOTO: String = "Добавте фото. " + SKIP
  lazy val ADD_MOTHER: String = "Добавте маму. Укажите ее ФИО. " + SKIP
  lazy val ADD_FATHER: String = "Добавте папу. Укажите его ФИО. " + SKIP
  lazy val ADD_PARTNER: String = "Добавте партнера (муж/жена). Укажите его/ее ФИО." + SKIP
  val SKIP: String = "Если хотите пропустить данный диалог, напишите /skip"
  val NOT_ALLOWED_SKIP: String = "В данный момент команда /skip не доступна. Поле обязательное."
  val NOT_CORRECT_DATE: String = "Некорректно введена дата."
  val SUCCESS_ADD: String = "Успешно добавлено древо."
  val NOT_FOUND_TREE_KEY: String = "Не найдено дерево с данным ключом."
  val UNKNOWN_NUMBER: String = "Некорректно передан номер."

  def outAncestry(ancestry: Ancestry, father: Option[Ancestry], mother: Option[Ancestry], partner: Option[Ancestry]): String = {
    s"ФИО: ${fullName(Some(ancestry))}\nДата рождения: ${formatDate(ancestry.birthDate)}\nМужчина?: ${ancestry.gender}\n" +
      s"Описание: ${getDesc(ancestry.description)}\nОтец: ${fullName(father)}\nМать: ${fullName(mother)}\n" +
      s"Партнер: ${fullName(partner)}"
  }

  def getDesc(desc : Option[String]) = desc match {
    case None => "Отсутствует"
    case Some(desc) => desc
  }

  def fullName(ancestry: Option[Ancestry]) = ancestry match {
    case None => "Отсутствует"
    case Some(ancestry) => s"${ancestry.secondName} ${ancestry.firstName} ${ancestry.middleName}"
  }

  def formatDate(date: Date) = new SimpleDateFormat("dd.MM.yyyy").format(date)
}
