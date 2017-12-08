package com.example.lagom.hello.api

import play.api.libs.json._

case class HelloRequest(name: String, description: String)

object HelloRequest {
  implicit val format: Format[HelloRequest] = Json.format[HelloRequest]
}
