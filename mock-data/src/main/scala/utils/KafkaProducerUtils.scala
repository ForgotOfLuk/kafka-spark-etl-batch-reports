package utils

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Failure, Success, Try}

import java.util.Properties

object KafkaProducerUtils extends LazyLogging {

  private def createProducerProperties(brokers: String, schemaRegistryUrl: String): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props
  }

  def createAvroProducer[T](brokers: String, schemaRegistryUrl: String): KafkaProducer[String, T] = {
    val props = createProducerProperties(brokers, schemaRegistryUrl)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
    new KafkaProducer[String, T](props)
  }

  def createStringProducer[T](brokers: String): KafkaProducer[String, T] = {
    val props = createProducerProperties(brokers, "")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    new KafkaProducer[String, T](props)
  }

  def sendRecord[T](producer: KafkaProducer[String, T], topic: String, key: String, record: T): Unit = {
    Try {
      val producerRecord = new ProducerRecord[String, T](topic, key, record)
      producer.send(producerRecord)
    } match {
      case Success(_) => logger.debug(s"Successfully sent record to $topic with key: $key")
      case Failure(e) => logger.error(s"Error in sending record to $topic: ${e.getMessage}", e)
    }
  }
}
