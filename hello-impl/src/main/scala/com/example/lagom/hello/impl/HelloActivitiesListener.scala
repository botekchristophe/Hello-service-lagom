package com.example.lagom.hello.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.example.lagom.hello.api.{HelloActivity, HelloService}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * This class is an example of how to listen to a specific topic outside of an endpoint implementation.
  */
class HelloActivitiesListener(helloService: HelloService) {

  private val log = LoggerFactory.getLogger(classOf[HelloActivitiesListener])

  helloService.helloActivities.subscribe.atLeastOnce(Flow[HelloActivity].mapAsync(Thread.activeCount()) { a: HelloActivity =>
      log.info(s"Activity created: ${a.toString}")
      Future.successful(Done)
  })
}
