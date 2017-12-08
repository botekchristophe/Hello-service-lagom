package com.example.lagom.hello.impl

import java.util.UUID

import akka.NotUsed
import com.example.lagom.hello.api.HelloOps._
import com.example.lagom.hello.api._
import com.example.lagom.hello.api.shared.{AuthenticationResponse, ErrorResponse, ErrorResponses, Marshaller}
import com.example.lagom.hello.impl.command._
import com.example.lagom.hello.impl.event.{HelloCreated, HelloDeleted, HelloEvent, HelloUpdated}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl(entity: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends HelloService with Marshaller {

  private def ref = entity.refFor[HelloEntity](HelloEntity.id)

  override def helloActivities: Topic[HelloActivity] =
    TopicProducer.taggedStreamWithOffset(HelloEvent.Tag.allTags.to[immutable.Seq]) { (tag, offset) =>
      entity.eventStream(tag, offset)
        .map(ev => ev.event match {
          case HelloCreated(r) =>
            (HelloActivity(UUID.randomUUID(), System.currentTimeMillis, ActionTypes.CREATED, "Hello"), ev.offset)
          case HelloUpdated(r) =>
            (HelloActivity(UUID.randomUUID(), System.currentTimeMillis, ActionTypes.UPDATED, "Hello"), ev.offset)
          case HelloDeleted(r) =>
            (HelloActivity(UUID.randomUUID(), System.currentTimeMillis, ActionTypes.DELETED, "Hello"), ev.offset)
      })
  }

  // mock for authentication, assuming your authentication is handled in a different service
  final val auth: Future[Either[ErrorResponse, AuthenticationResponse]] = Future.successful(Left(ErrorResponses.UnAuthorized("User not authorized")))

  override def getHellos: ServiceCall[NotUsed, Either[ErrorResponse, Iterable[Hello]]] =
    ServerServiceCall((_, _) =>
      auth
        .flatMap(_.fold[Future[Either[ErrorResponse, Iterable[Hello]]]](
          e => Future.successful(Left(e)),
          info => ref.ask(GetHellos).map(Right(_))))
        .map(_.marshall)
      )

  override def createHello: ServiceCall[HelloRequest, Either[ErrorResponse, Hello]] =
      ServerServiceCall((_, hello) =>
        auth
          .flatMap(_.fold(
            e => Future.successful(Left(e)),
            info => ref.ask(AddHello(hello.toHello))))
          .map(_.marshall)
      )

  override def readHello(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Hello]] =
    ServerServiceCall((_, _) =>
      auth
        .flatMap(_.fold(
          e => Future.successful(Left(e)),
          info => ref.ask(GetHello(id))))
        .map(_.marshall)
    )

  override def updateHello(id: UUID): ServiceCall[HelloUpdate, Either[ErrorResponse, Hello]] =
    ServerServiceCall((_, hello) =>
      auth
        .flatMap(_.fold(
          e => Future.successful(Left(e)),
          info => ref.ask(UpdateHello(id, hello))))
        .map(_.marshall)
    )

  override def deleteHello(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Hello]] =
    ServerServiceCall((_, _) =>
      auth
        .flatMap(_.fold(
          e => Future.successful(Left(e)),
          info => ref.ask(DeleteHello(id))))
        .map(_.marshall)
    )
}
