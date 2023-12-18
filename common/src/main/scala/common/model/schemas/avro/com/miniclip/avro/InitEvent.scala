/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.miniclip.avro

import scala.annotation.switch

final case class InitEvent(var eventType: EventTypeInit = EventTypeInit.init, var time: Long, var userId: String, var country: String, var platform: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(EventTypeInit.init, 0L, "", "", "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        eventType
      }.asInstanceOf[AnyRef]
      case 1 => {
        time
      }.asInstanceOf[AnyRef]
      case 2 => {
        userId
      }.asInstanceOf[AnyRef]
      case 3 => {
        country
      }.asInstanceOf[AnyRef]
      case 4 => {
        platform
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.eventType = {
        value
      }.asInstanceOf[EventTypeInit]
      case 1 => this.time = {
        value
      }.asInstanceOf[Long]
      case 2 => this.userId = {
        value.toString
      }.asInstanceOf[String]
      case 3 => this.country = {
        value.toString
      }.asInstanceOf[String]
      case 4 => this.platform = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = com.miniclip.avro.InitEvent.SCHEMA$
}

object InitEvent {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InitEvent\",\"namespace\":\"com.miniclip.avro\",\"fields\":[{\"name\":\"eventType\",\"type\":{\"type\":\"enum\",\"name\":\"EventTypeInit\",\"symbols\":[\"init\"]},\"default\":\"init\"},{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"userId\",\"type\":\"string\"},{\"name\":\"country\",\"type\":\"string\"},{\"name\":\"platform\",\"type\":\"string\"}]}")
}