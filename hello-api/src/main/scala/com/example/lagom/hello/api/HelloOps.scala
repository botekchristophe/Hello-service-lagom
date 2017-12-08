package com.example.lagom.hello.api

import java.util.UUID

object HelloOps {
  implicit class HelloOperations(hello: Hello) {
    def update(that: HelloUpdate): Hello =
      hello.copy(
        description = that.description)
  }

  implicit class HelloRequestOperations(hello: HelloRequest) {
    def toHello: Hello =
      Hello(
        UUID.randomUUID(),
        name = hello.name,
        description = hello.description)
  }
}
