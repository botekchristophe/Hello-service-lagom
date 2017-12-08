package com.example.lagom.hello.api.shared

import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, ResponseHeader}

import scala.collection.immutable
import scala.concurrent.Future
import scala.language.{implicitConversions, reflectiveCalls}
import scala.util.{Left, Right}

/**
  * Every rest service should extend this trait to manage marshalling
  */
trait Marshaller {

  // Implicit conversions for rest services

  implicit def errorToResponse[T](error: ErrorResponse): (ResponseHeader, Either[ErrorResponse, T]) =
    (ResponseHeader(error.code, MessageProtocol.empty, immutable.Seq.empty[(String, String)]), Left(error))

  implicit def redirectionToResponse(redirect: RedirectionResponse): (ResponseHeader, Either[ErrorResponse, RedirectionResponse]) =
    (ResponseHeader(redirect.code, MessageProtocol.empty, redirect.headers.to[collection.immutable.Seq]), Right(redirect))

  implicit def errorResponseToLeft[A](error: ErrorResponse): Either[ErrorResponse, A] = Left(error)
  implicit def errorResponseToFuture[A](error: ErrorResponse): Future[Either[ErrorResponse, A]] = Future.successful(Left(error))

  implicit def eitherMarshall[A]: Marshallable[Either[ErrorResponse, A]] = new Marshallable[Either[ErrorResponse, A]] {
    override def marshall(either: Either[ErrorResponse, A]): (ResponseHeader, Either[ErrorResponse, A]) =
      either match {
        case Left(e: ErrorResponse) => e //implicit conversion errorToResponse
        case right @ (Right(_)) => (ResponseHeaders.Ok, right)
      }
  }

  implicit val eitherUnitMarshall: Marshallable[Either[ErrorResponse, Unit]] = new Marshallable[Either[ErrorResponse, Unit]] {
    override def marshall(either: Either[ErrorResponse, Unit]): (ResponseHeader, Either[ErrorResponse, Unit]) =
      either match {
        case Left(e: ErrorResponse) => e //implicit conversion errorToResponse
        case right @ (Right(_)) => (ResponseHeaders.Accepted, Right(Unit))
      }
  }

  implicit val eitherRedirectionMarshall: Marshallable[Either[ErrorResponse, RedirectionResponse]] =
    new Marshallable[Either[ErrorResponse, RedirectionResponse]] {
      override def marshall(either: Either[ErrorResponse, RedirectionResponse]): (ResponseHeader, Either[ErrorResponse, RedirectionResponse]) =
        either match {
          case Left(e: ErrorResponse) => e //implicit conversion errorToResponse
          case Right(r: RedirectionResponse) => r //implicit convertion redirectionToResponse
        }
    }

  implicit class MarshallOps[A](val a: A) {
    def marshall(implicit instance: Marshallable[A]): (ResponseHeader, A) =
      instance.marshall(a)
  }

  trait Marshallable[A] {
    def marshall(a: A): (ResponseHeader, A)
  }
}

