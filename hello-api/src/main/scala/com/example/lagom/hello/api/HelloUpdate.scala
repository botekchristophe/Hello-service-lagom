package com.example.lagom.hello.api

import play.api.libs.json._

case class HelloUpdate(description: String)

object HelloUpdate {
  implicit val format: Format[HelloUpdate] = Json.format[HelloUpdate]
}
