package com.example.lagom.hello.impl.command

import java.util.UUID

import com.example.lagom.hello.api.shared.ErrorResponse
import com.example.lagom.hello.api.{Hello, HelloState, HelloUpdate}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

/**
  * This interface defines all the commands that the Hello entity supports.
  */
sealed trait HelloCommand[R] extends ReplyType[R]

case object GetHellos extends HelloCommand[Set[Hello]]
case object GetState extends HelloCommand[HelloState]

case class AddHello(hello: Hello) extends HelloCommand[Either[ErrorResponse, Hello]]
object AddHello {
  implicit val format: Format[AddHello] = Json.format[AddHello]
}

case class GetHello(id: UUID) extends HelloCommand[Either[ErrorResponse, Hello]]
object GetHello {
  implicit val format: Format[GetHello] = Json.format[GetHello]
}

case class UpdateHello(id: UUID, hello: HelloUpdate) extends HelloCommand[Either[ErrorResponse, Hello]]
object UpdateHello {
  implicit val format: Format[UpdateHello] = Json.format[UpdateHello]
}

case class DeleteHello(id: UUID) extends HelloCommand[Either[ErrorResponse, Hello]]
object DeleteHello {
  implicit val format: Format[DeleteHello] = Json.format[DeleteHello]
}
