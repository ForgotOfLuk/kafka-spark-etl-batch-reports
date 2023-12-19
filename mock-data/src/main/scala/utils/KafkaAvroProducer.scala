package utils

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import com.typesafe.scalalogging.LazyLogging
import scala.util.Try

import java.util.Properties

object KafkaAvroProducer extends LazyLogging {

  def createProducer[T](brokers: String, schemaRegistryUrl: String): KafkaProducer[String, T] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

    // Enable exactly-once semantics
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    props.put(ProducerConfig.ACKS_CONFIG, "all")

    val producer = new KafkaProducer[String, T](props)
    producer
  }

  def sendRecord[T](producer: KafkaProducer[String, T], topic: String, key: String, record: T): Unit = {
    Try {
      val producerRecord = new ProducerRecord[String, T](topic, key, record)
      producer.send(producerRecord)
      logger.debug(s"Successfully sent record to $topic with key: $key")
    } recover {
      case e: Exception =>
        logger.error(s"Error in sending record to $topic: ${e.getMessage}", e)
    }
  }
}
