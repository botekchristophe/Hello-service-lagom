package com.example.lagom.hello.api

import java.util.UUID

import akka.NotUsed
import com.example.lagom.hello.api.serialization.JsonFormats._
import com.example.lagom.hello.api.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.{Environment, Mode}

trait HelloService extends Service {

  final val api: String = "/api/rest/1.0"

  def getHellos: ServiceCall[NotUsed, Either[ErrorResponse, Iterable[Hello]]]
  def createHello: ServiceCall[HelloRequest, Either[ErrorResponse, Hello]]
  def readHello(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Hello]]
  def updateHello(id: UUID): ServiceCall[HelloUpdate, Either[ErrorResponse, Hello]]
  def deleteHello(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Hello]]

  def helloActivities: Topic[HelloActivity]


  override final def descriptor: Descriptor = {
  import Service._
  // @formatter:off
  named("hello-service").withCalls(
    restCall(Method.GET,    s"$api/hellos", getHellos _),
    restCall(Method.POST,   s"$api/hellos/hello", createHello _),
    restCall(Method.GET,    s"$api/hellos/hello/:id", readHello _),
    restCall(Method.PATCH,  s"$api/hellos/hello/:id", updateHello _),
    restCall(Method.DELETE, s"$api/hellos/hello/:id", deleteHello _)
    )
  .withAutoAcl(true)
  .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
  .withTopics(topic("Hello-topic", helloActivities))
  // @formatter:on
  }
}
