package com.example.lagom.hello.api

import java.util.UUID

import com.example.lagom.hello.api.ActionTypes.ActionType
import com.example.lagom.hello.api.serialization.JsonFormats
import play.api.libs.json.{Format, Json}


case class HelloActivity(id: UUID,
                         timestamp: Long,
                         actionType: ActionType,
                         message: String)

object ActionTypes extends Enumeration {
  val CREATED, UPDATED, DELETED = Value
  type ActionType = Value
  implicit val format: Format[ActionType] = JsonFormats.enumFormat(ActionTypes)
}

object Activity {
  implicit val format: Format[HelloActivity] = Json.format[HelloActivity]
}


