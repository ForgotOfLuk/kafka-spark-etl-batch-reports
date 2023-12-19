package model

import com.miniclip.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import org.apache.kafka.clients.producer.KafkaProducer

case class KafkaProducers(
    initEventProducer: KafkaProducer[String, InitEvent],
    matchEventProducer: KafkaProducer[String, MatchEvent],
    inAppPurchaseEventProducer: KafkaProducer[String, InAppPurchaseEvent]
) {
  def closeProducers(): Unit = {
    initEventProducer.close()
    matchEventProducer.close()
    inAppPurchaseEventProducer.close()
  }
}