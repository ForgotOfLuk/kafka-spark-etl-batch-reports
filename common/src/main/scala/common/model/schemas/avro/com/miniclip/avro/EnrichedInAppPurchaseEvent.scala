/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.miniclip.avro

import scala.annotation.switch

final case class EnrichedInAppPurchaseEvent(var eventType: EventTypeInAppPurchase = EventTypeInAppPurchase.in_app_purchase, var time: Long, var purchaseValue: Double, var userId: String, var productId: String, var country: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(EventTypeInAppPurchase.in_app_purchase, 0L, 0.0, "", "", "")
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
      case 5 => {
        country
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
        value.toString
      }.asInstanceOf[String]
      case 4 => this.productId = {
        value.toString
      }.asInstanceOf[String]
      case 5 => this.country = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = com.miniclip.avro.EnrichedInAppPurchaseEvent.SCHEMA$
}

object EnrichedInAppPurchaseEvent {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EnrichedInAppPurchaseEvent\",\"namespace\":\"com.miniclip.avro\",\"fields\":[{\"name\":\"eventType\",\"type\":{\"type\":\"enum\",\"name\":\"EventTypeInAppPurchase\",\"symbols\":[\"in_app_purchase\"]},\"default\":\"in_app_purchase\"},{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"purchaseValue\",\"type\":\"double\"},{\"name\":\"userId\",\"type\":\"string\"},{\"name\":\"productId\",\"type\":\"string\"},{\"name\":\"country\",\"type\":\"string\"}]}")
}