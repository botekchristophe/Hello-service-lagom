package com.example.lagom.hello

import com.typesafe.config.{Config, ConfigFactory}

package object impl {
  val config: Config = ConfigFactory.load()

  //load some configuration here
}