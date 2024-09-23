package common.kafka

import com.leca.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import org.apache.kafka.clients.producer.KafkaProducer

case class KafkaProducers(
    initEventProducer: KafkaProducer[String, InitEvent],
    matchEventProducer: KafkaProducer[String, MatchEvent],
    inAppPurchaseEventProducer: KafkaProducer[String, InAppPurchaseEvent],
    globalKtableProducers: KafkaProducer[String, String]
) {
  def closeProducers(): Unit = {
    initEventProducer.close()
    matchEventProducer.close()
    inAppPurchaseEventProducer.close()
    globalKtableProducers.close()
  }
}