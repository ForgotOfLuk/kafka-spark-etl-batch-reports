/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.miniclip.avro

import scala.annotation.switch

final case class EnrichedMatchEvent(var eventType: EventTypeMatch = EventTypeMatch.`match`, var time: Long, var userA: String, var userB: String, var userAPostmatchInfo: com.miniclip.avro.UserPostmatchInfo, var userBPostmatchInfo: Option[com.miniclip.avro.UserPostmatchInfo] = None, var winner: String, var gameTier: Long, var duration: Long, var countryA: String, var countryB: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(EventTypeMatch.`match`, 0L, "", "", new UserPostmatchInfo, None, "", 0L, 0L, "", "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        eventType
      }.asInstanceOf[AnyRef]
      case 1 => {
        time
      }.asInstanceOf[AnyRef]
      case 2 => {
        userA
      }.asInstanceOf[AnyRef]
      case 3 => {
        userB
      }.asInstanceOf[AnyRef]
      case 4 => {
        userAPostmatchInfo
      }.asInstanceOf[AnyRef]
      case 5 => {
        userBPostmatchInfo match {
          case Some(x) => x
          case None => null
        }
      }.asInstanceOf[AnyRef]
      case 6 => {
        winner
      }.asInstanceOf[AnyRef]
      case 7 => {
        gameTier
      }.asInstanceOf[AnyRef]
      case 8 => {
        duration
      }.asInstanceOf[AnyRef]
      case 9 => {
        countryA
      }.asInstanceOf[AnyRef]
      case 10 => {
        countryB
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.eventType = {
        value
      }.asInstanceOf[EventTypeMatch]
      case 1 => this.time = {
        value
      }.asInstanceOf[Long]
      case 2 => this.userA = {
        value.toString
      }.asInstanceOf[String]
      case 3 => this.userB = {
        value.toString
      }.asInstanceOf[String]
      case 4 => this.userAPostmatchInfo = {
        value
      }.asInstanceOf[com.miniclip.avro.UserPostmatchInfo]
      case 5 => this.userBPostmatchInfo = {
        value match {
          case null => None
          case _ => Some(value)
        }
      }.asInstanceOf[Option[com.miniclip.avro.UserPostmatchInfo]]
      case 6 => this.winner = {
        value.toString
      }.asInstanceOf[String]
      case 7 => this.gameTier = {
        value
      }.asInstanceOf[Long]
      case 8 => this.duration = {
        value
      }.asInstanceOf[Long]
      case 9 => this.countryA = {
        value.toString
      }.asInstanceOf[String]
      case 10 => this.countryB = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = com.miniclip.avro.EnrichedMatchEvent.SCHEMA$
}

object EnrichedMatchEvent {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EnrichedMatchEvent\",\"namespace\":\"com.miniclip.avro\",\"fields\":[{\"name\":\"eventType\",\"type\":{\"type\":\"enum\",\"name\":\"EventTypeMatch\",\"symbols\":[\"match\"]},\"default\":\"match\"},{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"userA\",\"type\":\"string\"},{\"name\":\"userB\",\"type\":\"string\"},{\"name\":\"userAPostmatchInfo\",\"type\":{\"type\":\"record\",\"name\":\"UserPostmatchInfo\",\"fields\":[{\"name\":\"coinBalanceAfterMatch\",\"type\":\"long\"},{\"name\":\"levelAfterMatch\",\"type\":\"long\"},{\"name\":\"device\",\"type\":\"string\"},{\"name\":\"platform\",\"type\":\"string\"}]}},{\"name\":\"userBPostmatchInfo\",\"type\":[\"null\",\"UserPostmatchInfo\"],\"default\":null},{\"name\":\"winner\",\"type\":\"string\"},{\"name\":\"gameTier\",\"type\":\"long\"},{\"name\":\"duration\",\"type\":\"long\"},{\"name\":\"countryA\",\"type\":\"string\"},{\"name\":\"countryB\",\"type\":\"string\"}]}")
}