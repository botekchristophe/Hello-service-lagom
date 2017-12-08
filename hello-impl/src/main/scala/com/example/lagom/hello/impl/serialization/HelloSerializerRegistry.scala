package com.example.lagom.hello.impl.serialization

import com.example.lagom.hello.api.{Hello, HelloState}
import com.example.lagom.hello.impl.command.{AddHello, DeleteHello, GetHello, UpdateHello}
import com.example.lagom.hello.impl.event.{HelloCreated, HelloDeleted, HelloUpdated}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object HelloSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // Command
    JsonSerializer[AddHello],
    JsonSerializer[GetHello],
    JsonSerializer[UpdateHello],
    JsonSerializer[DeleteHello],
    // Event
    JsonSerializer[HelloCreated],
    JsonSerializer[HelloUpdated],
    JsonSerializer[HelloDeleted],
    // Model
    JsonSerializer[HelloState],
    JsonSerializer[Hello]
  )
}
