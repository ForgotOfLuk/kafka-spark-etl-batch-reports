/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.leca.avro

import scala.annotation.switch

final case class UserPostmatchInfo(var coinBalanceAfterMatch: Long, var levelAfterMatch: Long, var device: String, var platform: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(0L, 0L, "", "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        coinBalanceAfterMatch
      }.asInstanceOf[AnyRef]
      case 1 => {
        levelAfterMatch
      }.asInstanceOf[AnyRef]
      case 2 => {
        device
      }.asInstanceOf[AnyRef]
      case 3 => {
        platform
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.coinBalanceAfterMatch = {
        value
      }.asInstanceOf[Long]
      case 1 => this.levelAfterMatch = {
        value
      }.asInstanceOf[Long]
      case 2 => this.device = {
        value.toString
      }.asInstanceOf[String]
      case 3 => this.platform = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = com.leca.avro.UserPostmatchInfo.SCHEMA$
}

object UserPostmatchInfo {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"UserPostmatchInfo\",\"namespace\":\"com.leca.avro\",\"fields\":[{\"name\":\"coinBalanceAfterMatch\",\"type\":\"long\"},{\"name\":\"levelAfterMatch\",\"type\":\"long\"},{\"name\":\"device\",\"type\":\"string\"},{\"name\":\"platform\",\"type\":\"string\"}]}")
}