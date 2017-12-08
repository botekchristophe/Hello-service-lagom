package com.example.lagom.hello.api.shared

import play.api.libs.json.{Format, Json}

case class AuthenticationResponse(user: String)
object AuthenticationResponse {
  implicit val format: Format[AuthenticationResponse] = Json.format[AuthenticationResponse]
}