package models

import java.sql.Date

import play.api.libs.json.{Json, OFormat}

case class Account(
                  id: Long,
                  email: String,
                  password: String,
                  category: String,
                  city: String,
                  state: String,
                  createDate: Date
                  )

object Account {
  implicit val format: OFormat[Account] = Json.format[Account]
  }
