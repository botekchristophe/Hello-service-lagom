package com.example.lagom.hello.impl

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.example.lagom.hello.api.HelloCassandra
import com.example.lagom.hello.impl.event.{HelloCreated, HelloDeleted, HelloEvent, HelloUpdated}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class HelloReadSideProcessor(readSide: CassandraReadSide, session: CassandraSession)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[HelloEvent] {

  private val log = LoggerFactory.getLogger(classOf[HelloReadSideProcessor])

  private var insertHelloStatement: PreparedStatement = _
  private var updateHelloStatement: PreparedStatement = _
  private var deleteHelloStatement: PreparedStatement = _


  def buildHandler: ReadSideHandler[HelloEvent] = {
    readSide.builder[HelloEvent]("hellosOffset")
      .setGlobalPrepare(createTable)
      .setPrepare { tag =>
        prepareStatements()
      }.setEventHandler[HelloCreated](insertHello)
      .setEventHandler[HelloDeleted](deleteHello)
      .setEventHandler[HelloUpdated](updateHello)
      .build()
  }

  private def createTable(): Future[Done] = {
    log.debug(s"CREATE TABLE: " + HelloCassandra.createTable)
    for {
      _ <- session.executeCreateTable(HelloCassandra.createTable)
    } yield Done
  }

  private def prepareStatements(): Future[Done] = {
    for {
      insert <- session.prepare(HelloCassandra.insert)
      delete <- session.prepare(HelloCassandra.delete)
      update <- session.prepare(HelloCassandra.update)
    } yield {
      insertHelloStatement = insert
      log.debug(s"INSERT: " + HelloCassandra.insert)
      updateHelloStatement = update
      log.debug(s"UPDATE: " + HelloCassandra.update)
      deleteHelloStatement = delete
      log.debug(s"DELETE: " + HelloCassandra.delete)
      Done
    }
  }

  private def insertHello(created: EventStreamElement[HelloCreated]) = {
    log.info("Cassandra received INSERT event")
    Future.successful {
      val r = created.event.hello
      List(insertHelloStatement.bind(
        r.id.toString,
        r.name,
        r.description
      ))
    }
  }

  private def deleteHello(deleted: EventStreamElement[HelloDeleted]) = {
    log.info("Cassandra received DELETE event")
    Future.successful(
      List(deleteHelloStatement.bind(deleted.event.hello.id.toString)
      ))
  }

  private def updateHello(updated: EventStreamElement[HelloUpdated]) = {
    log.info("Cassandra received UPDATE event")
    Future.successful {
      val r = updated.event.hello
      List(updateHelloStatement.bind(
        r.name,
        r.description,
        r.id.toString
      ))
    }
  }

  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] = HelloEvent.Tag.allTags
}



