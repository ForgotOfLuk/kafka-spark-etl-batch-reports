package common.kafka

import com.miniclip.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import org.apache.kafka.clients.consumer.KafkaConsumer

case class KafkaConsumers(
    initEventConsumer: KafkaConsumer[String, InitEvent],
    matchEventConsumer: KafkaConsumer[String, MatchEvent],
    inAppPurchaseEventConsumer: KafkaConsumer[String, InAppPurchaseEvent],
    globalKtableConsumers: KafkaConsumer[String, String]
) {
  def closeConsumers(): Unit = {
    initEventConsumer.close()
    matchEventConsumer.close()
    inAppPurchaseEventConsumer.close()
    globalKtableConsumers.close()
  }
}