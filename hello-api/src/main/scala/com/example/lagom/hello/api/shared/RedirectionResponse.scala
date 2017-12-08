package com.example.lagom.hello.api.shared

import play.api.libs.json.{Format, Json}

/**
  * Common model to display an redirection.
  *
  * @param code status code as an integer
  * @param headers sequense of headers for the redirection
  */
case class RedirectionResponse(code: Int, headers: Map[String, String])

object RedirectionResponse {
  implicit val format: Format[RedirectionResponse] = Json.format[RedirectionResponse]
}

object RedirectionResponses {
  //scalastyle:off
  val NotModified: RedirectionResponse = RedirectionResponse(304, Map.empty[String, String])
  val Found: Map[String, String] => RedirectionResponse = { headers => RedirectionResponse(302, headers) }
  //scalastyle:on
}

