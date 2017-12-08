package com.example.lagom.hello.api.shared

import scala.collection.immutable.{List, ListMap}
import scala.reflect.runtime.universe._

/**
  * This trait act as an helper to use Cassandra and CQL syntax.
  * Example of usage :
  * <code>
  *   object MyObjectCassandra extends CassandraHelper[CaseClass] {
  *   override val tableName = ...
  *   }
  *
  *   then be called as is :
  *
  *   MyObjectCassandra.createTable
  * </code>
  * @tparam BASE base object used as model for creation and selection.
  */
trait CassandraHelper[BASE <: DatabaseModel] {

  /**
    * Table name in cassandra
    */
  val tableName: String

  /**
    * Name of the table that contains offesets in Cassandra.
    * By default {tableName} + "OffSet".
    */
  val offSetTable: String = s"${tableName}OffSet"

  /**
    * Primary index is most of the time called "id"
    * but can be overridden to set a custom value
    */
  val primaryIndex: String = "id"

  /**
    * The list of secondary indices. (fieldName -> indexName)
    */
  val secondaryIndex: ListMap[String, String] = ListMap.empty[String, String]


  /**
    * Generates a CQL query for create table along with its primary index.
    */
  def createTable(implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    s"""
       |CREATE TABLE IF NOT EXISTS $tableName (
       |$fieldsWithType
       |PRIMARY KEY ($primaryIndex)
       |)
    """.stripMargin

  /**
    * Generates a CQL query for creating a secondary index on a table.
    */
  def createIndex(index: (String, String)): String =
    s"""
       |CREATE INDEX IF NOT EXISTS ${index._2}
       |ON $tableName(${index._1})
    """.stripMargin

  /**
    * Generates a CQL query for inserting a specific object.
    */
  def insert(implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    s"""
       |INSERT INTO $tableName
       |$fieldsName
       |VALUES $fieldsPlaceholder
    """.stripMargin

  /**
    * Generates a CQL query for updating a specific object.
    */
  def update(implicit lin: List[BASE] = List.empty[BASE], evAin: TypeTag[BASE]): String =
    s"""
       |UPDATE $tableName
       |SET $fieldsNameEqPlaceHolder
       |WHERE id = ?
    """.stripMargin

  /**
    * Generates a CQL query for deleting a specific object.
    */
  def delete: String =
    s"""
       |DELETE
       |FROM $tableName
       |WHERE id = ?
    """.stripMargin

  /**
    * Generates a CQL query for selecting all objects of a specific table.
    */
  def selectAll: String =
    s"""
       |SELECT * FROM $tableName
    """.stripMargin

  /**
    * Generates a CQL query for selecting one object.
    */
  def selectOne(idValue: String): String =
    s"""
       |SELECT * FROM $tableName WHERE $primaryIndex = $idValue
    """.stripMargin

  /**
    * Generates a CQL query for selecting all ids of a specific table.
    */
  def selectIds: String =
    s"""
       |SELECT $primaryIndex FROM $tableName
    """.stripMargin

  /**
    * Fetching the members of the model.
    */
  private val fetchFields: Type => Iterable[Symbol] = _.members.filterNot(_.isMethod)

  /**
    * Define all fields for a specific cassandra table.
    * where the key is the field name and value the class
    * of this field in cassandra representation.
    *
    * /!\ The order of the fields matters. /!\
    *
    * @return all fields associated with their type
    *         as [[scala.collection.immutable.Map[String, String]]
    */
  private def fields (implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]) : ListMap[String, String] = {
    val iterable = fetchFields(typeOf[BASE])
      .map { model =>
        val fieldType = "text"
        model.name.toString -> fieldType
      }.toSeq
      .reverse
    ListMap(iterable: _*)
  }

  /**
    * Define a sequence of mutable field for this object.
    *
    * @return mutable fiels as [[scala.collection.immutable.Seq[String]]
    */
  private def mutableFields (implicit lin: List[BASE] = List.empty[BASE], evAin: TypeTag[BASE]) : Seq[String] = {
    fetchFields(typeOf[BASE])
      .map(_.name.toString.trim)
      .filterNot(_.equals(primaryIndex))
      .toSeq
      .reverse
  }

  private def fieldsTo(transform : String => String)(implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    fields.keySet.toList.map(transform).mkString("(", ", ", ")")

  private def fieldsName (implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    fieldsTo(f => f)

  private def fieldsPlaceholder (implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    fieldsTo(f => "?")

  private def fieldsNameEqPlaceHolder (implicit lin: List[BASE] = List.empty[BASE], evAin: TypeTag[BASE]): String =
    mutableFields.map(f => s"$f = ?").mkString(", ")

  private def fieldsWithType (implicit l: List[BASE] = List.empty[BASE], evA: TypeTag[BASE]): String =
    fields.map { case (k, v) => s"${k.dropRight(1)} $v"}.mkString("", ",", ",")
}

