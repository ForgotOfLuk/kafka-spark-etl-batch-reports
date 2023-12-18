import com.miniclip.avro.{EventTypeInit, InitEvent}
import common.KafkaAvroProducer
import org.apache.kafka.clients.producer.ProducerRecord

object Main {
  def main(args: Array[String]): Unit = {
    val brokers = "localhost:9101"
    val schemaRegistryUrl = "http://localhost:8081"
    val topic = "init"

    // Create a Kafka Avro producer
    val producer = KafkaAvroProducer.createProducer[InitEvent](brokers, schemaRegistryUrl)

    // Create an instance of InitEvent
    val initEvent = InitEvent(EventTypeInit.init, System.currentTimeMillis(), "user123", "US", "Android")

    // Send the InitEvent message to the Kafka topic
    producer.send(new ProducerRecord[String, InitEvent](topic, "init-event-key", initEvent))

    // Close the producer when done
    producer.close()
  }
}