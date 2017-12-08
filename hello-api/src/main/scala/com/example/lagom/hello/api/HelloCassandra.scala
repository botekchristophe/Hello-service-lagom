package com.example.lagom.hello.api

import com.example.lagom.hello.api.shared.CassandraHelper

object HelloCassandra extends CassandraHelper[Hello] {

  override val tableName = "hellos"
  override val primaryIndex = "id"
}
