package com.example.lagom.hello.impl.event

import com.example.lagom.hello.api.Hello
import com.example.lagom.hello.api.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

trait HelloEvent extends AggregateEvent[HelloEvent] {
  override def aggregateTag: AggregateEventTagger[HelloEvent] = HelloEvent.Tag
}

object HelloEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[HelloEvent] = AggregateEventTag.sharded[HelloEvent](NumShards)
}

case class HelloCreated(hello: Hello) extends HelloEvent
object HelloCreated {
  implicit val format: Format[HelloCreated] = Json.format[HelloCreated]
}

case class HelloUpdated(hello: Hello) extends HelloEvent
object HelloUpdated {
  implicit val format: Format[HelloUpdated] = Json.format[HelloUpdated]
}

case class HelloDeleted(hello: Hello) extends HelloEvent
object HelloDeleted {
  implicit val format: Format[HelloDeleted] = Json.format[HelloDeleted]
}

case class ErrorEvent(e: ErrorResponse) extends HelloEvent
object ErrorEvent {
  implicit val format: Format[ErrorEvent] = Json.format[ErrorEvent]
}
