package com.example.lagom.hello.impl

import java.util.UUID

import cats.syntax.either._
import com.example.lagom.hello.api.HelloOps._
import com.example.lagom.hello.api.shared.{ErrorResponse, ErrorResponses => ER}
import com.example.lagom.hello.api.{Hello, HelloState}
import com.example.lagom.hello.impl.command._
import com.example.lagom.hello.impl.event._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.slf4j.LoggerFactory

object HelloEntity {
  final val id: String = UUID.randomUUID().toString
}

class HelloEntity extends PersistentEntity {

  override type Command = HelloCommand[_]
  override type Event = HelloEvent
  override type State = HelloState

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: HelloState = HelloState(Set.empty[Hello])

  var currentState: HelloState = initialState

  private val log = LoggerFactory.getLogger(classOf[HelloEntity])

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case HelloState(message) => Actions()
      .onReadOnlyCommand[GetHellos.type, Set[Hello]] {
      readOnlyCommandHandler.asInstanceOf[ReadOnlyHandler[Set[Hello]]]

    }.onReadOnlyCommand[GetState.type, HelloState] {
      readOnlyCommandHandler.asInstanceOf[ReadOnlyHandler[HelloState]]

    }.onReadOnlyCommand[GetHello, Either[ErrorResponse, Hello]] {
      readOnlyCommandHandler.asInstanceOf[ReadOnlyHandler[Either[ErrorResponse, Hello]]]

    }
      .onCommand[AddHello, Either[ErrorResponse, Hello]] { onCommandHandler }
      .onCommand[UpdateHello, Either[ErrorResponse, Hello]] { onCommandHandler }
      .onCommand[DeleteHello, Either[ErrorResponse, Hello]] { onCommandHandler }
      .onEvent { eventHandler }
  }

  /**
    * Manage all readOnlyCommands received.
    */
  val readOnlyCommandHandler: ReadOnlyHandler[Any] = {
    case (GetHellos, ctx, state) =>
      ctx.reply(state.hellos)

    case (GetState, ctx, state) =>
      ctx.reply(state)

    case (GetHello(helloId), ctx, state) =>
      val hello = state.hellos.find(_.id == helloId)
        .toRight(ER.NotFound(Hello +" not found with id=" + helloId))
      ctx.reply(hello)
  }

  /**
    * manage all command received.
    */
  val onCommandHandler: OnCommandHandler[Either[ErrorResponse, Hello]] = {
    case (AddHello(hello), ctx, state) =>
      state.hellos.find(_.name == hello.name)
        .toLeft(hello)
        .leftMap(_ => ER.Conflict("Hello already exists."))
        .fold(
        { e => ctx.thenPersist(ErrorEvent(e)) {_ => ctx.reply(Left(e)) }},
        { r => ctx.thenPersist(HelloCreated(r)) {_ => ctx.reply(Right(r)) }})

    case (UpdateHello(id, hello), ctx, state) =>
      state.hellos.find(_.id == id)
        .toRight(ER.NotFound("Hello not found."))
        .fold(
        { e => ctx.thenPersist(ErrorEvent(e)) {_ => ctx.reply(Left(e)) }},
        { r => ctx.thenPersist(HelloUpdated(r.update(hello))) {_ => ctx.reply(Right(r.update(hello))) }})

    case (DeleteHello(id), ctx, state) =>
      state.hellos.find(_.id == id)
        .toRight(ER.NotFound("Hello not found."))
        .fold(
        { e => ctx.thenPersist(ErrorEvent(e)) {_ => ctx.reply(Left(e)) }},
        { hello => ctx.thenPersist(HelloDeleted(hello)) {_ => ctx.reply(Right(hello)) }})
  }


  /**
    * Manage all event received
    */
  val eventHandler: EventHandler = {
    case (HelloCreated(hello), state) =>
      log.info("Persistence actor received a CREATED EVENT")
      state.copy(hellos = state.hellos + hello)

    case (HelloDeleted(a), state) =>
      log.info("Persistence actor received a DELETE EVENT")
      state.copy(hellos = state.hellos.filterNot(hello => hello.id == a.id))

    case (HelloUpdated(a), state) =>
      log.info("Persistence actor received a UPDATE EVENT")
      val hellos = state.hellos.filterNot(hello => hello.id == a.id)
      state.copy(hellos = hellos + a)

    case (ErrorEvent(e), _) =>
      log.info(e.toString)
      currentState
  }
}
