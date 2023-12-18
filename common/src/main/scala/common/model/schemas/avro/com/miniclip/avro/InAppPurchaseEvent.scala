/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.miniclip.avro

import scala.annotation.switch

final case class InAppPurchaseEvent(var eventType: EventTypeInAppPurchase = EventTypeInAppPurchase.in_app_purchase, var time: Long, var purchaseValue: Double, var userId: Either[String, Long], var productId: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(EventTypeInAppPurchase.in_app_purchase, 0L, 0.0, Left(""), "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        eventType
      }.asInstanceOf[AnyRef]
      case 1 => {
        time
      }.asInstanceOf[AnyRef]
      case 2 => {
        purchaseValue
      }.asInstanceOf[AnyRef]
      case 3 => {
        userId
      }.asInstanceOf[AnyRef]
      case 4 => {
        productId
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.eventType = {
        value
      }.asInstanceOf[EventTypeInAppPurchase]
      case 1 => this.time = {
        value
      }.asInstanceOf[Long]
      case 2 => this.purchaseValue = {
        value
      }.asInstanceOf[Double]
      case 3 => this.userId = {
        value
      }.asInstanceOf[Either[String, Long]]
      case 4 => this.productId = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = com.miniclip.avro.InAppPurchaseEvent.SCHEMA$
}

object InAppPurchaseEvent {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InAppPurchaseEvent\",\"namespace\":\"com.miniclip.avro\",\"fields\":[{\"name\":\"eventType\",\"type\":{\"type\":\"enum\",\"name\":\"EventTypeInAppPurchase\",\"symbols\":[\"in_app_purchase\"]},\"default\":\"in_app_purchase\"},{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"purchaseValue\",\"type\":\"double\"},{\"name\":\"userId\",\"type\":[\"string\",\"long\"]},{\"name\":\"productId\",\"type\":\"string\"}]}")
}