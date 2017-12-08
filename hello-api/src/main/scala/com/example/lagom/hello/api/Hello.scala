package com.example.lagom.hello.api

import java.util.UUID

import com.example.lagom.hello.api.shared.DatabaseModel
import play.api.libs.json._

case class Hello(id: UUID, name: String, description: String) extends DatabaseModel

object Hello {
  implicit val format: Format[Hello] = Json.format[Hello]
}