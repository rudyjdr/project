package models

import play.api.libs.json.{ Json, OFormat }

case class Account(
  id: Long,
  email: String,
  password: String,
  category: String,
  city: String
)

object Account {
  implicit val format: OFormat[Account] = Json.format[Account]
}
