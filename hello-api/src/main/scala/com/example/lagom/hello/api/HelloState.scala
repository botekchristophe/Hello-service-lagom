package com.example.lagom.hello.api

import play.api.libs.json._

case class HelloState(hellos: Set[Hello])

object HelloState {
  implicit val format: Format[HelloState] = Json.format[HelloState]
}