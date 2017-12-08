package com.example.lagom.hello.api.shared

import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, ResponseHeader}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable

/**
  * Common model to display an error.
  *
  * @param code status code as an integer
  * @param error status code as a string
  * @param message Short description of the error that occurred.
  */
case class ErrorResponse(code: Int, error: String, message: String)

object ErrorResponse {
  implicit val format: Format[ErrorResponse] = Json.format[ErrorResponse]
}

object ErrorResponses {
  //scalastyle:off
  val BadRequest: String => ErrorResponse = { message => ErrorResponse(400, "BadRequest", message) }
  val UnAuthorized: String => ErrorResponse = { message => ErrorResponse(401, "UnAuthorized", message) }
  val PaymentRequired: String => ErrorResponse = { message => ErrorResponse(402, "Payment Required", message) }
  val Forbidden: String => ErrorResponse = { message => ErrorResponse(403, "Forbidden", message) }
  val NotFound: String => ErrorResponse = { message => ErrorResponse(404, "Not Found", message) }
  val Conflict: String => ErrorResponse = { message => ErrorResponse(409, "Conflict", message) }
  val UnProcessable: String => ErrorResponse = { message => ErrorResponse(422, "UnProcessable", message) }
  val InternalServerError: String => ErrorResponse = { message => ErrorResponse(500, "Internal Server Error", message) }
  val NotImplemented: String => ErrorResponse = { message => ErrorResponse(501, "Not implemented", message) }
  val ServiceUnavailableError: String => ErrorResponse = { message => ErrorResponse(503, "Service Unavailable", message) }
  //scalastyle:on
}

object ResponseHeaders {
  //scalastyle:off
  private val emptyStatus: Int => ResponseHeader = { code =>
    ResponseHeader(code, MessageProtocol.empty, immutable.Seq.empty[(String, String)])
  }

  val Ok = ResponseHeader.Ok
  val Created = emptyStatus(201)
  val Accepted = emptyStatus(202)
  val NoContent = emptyStatus(204)
  val BadRequest = emptyStatus(400)
  val UnAuthorized = emptyStatus(401)
  val PaymentRequired = emptyStatus(402)
  val Forbidden = emptyStatus(403)
  val NotFound = emptyStatus(404)
  val Conflict = emptyStatus(409)
  val UnProcessable = emptyStatus(422)
  val InternalServerError = emptyStatus(500)

  //scalastyle:on
}
