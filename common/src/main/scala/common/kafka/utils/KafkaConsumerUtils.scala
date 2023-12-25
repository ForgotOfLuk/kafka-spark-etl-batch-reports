package common.kafka.utils

import com.typesafe.scalalogging.LazyLogging
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.StringSerializer

import java.time.Duration
import java.util.Properties
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

object KafkaConsumerUtils extends LazyLogging {

  private def createConsumerProperties(brokers: String, schemaRegistryUrl: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    props
  }

  def createAvroConsumer[T](brokers: String, schemaRegistryUrl: String): KafkaConsumer[String, T] = {
    val props = createConsumerProperties(brokers, schemaRegistryUrl)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
    new KafkaConsumer[String, T](props)
  }

  def createStringConsumer[T](brokers: String): KafkaConsumer[String, T] = {
    val props = createConsumerProperties(brokers, "")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    new KafkaConsumer[String, T](props)
  }

  def consumeRecords[T](consumer: KafkaConsumer[String, T], topic: String, timeout: Duration): Try[List[ConsumerRecord[String, T]]] = {
    Try {
      consumer.subscribe(java.util.Collections.singletonList(topic))
      val records: ConsumerRecords[String, T] = consumer.poll(timeout)
      records.iterator().asScala.toList
    } match {
      case Success(records) =>
        logger.debug(s"Successfully consumed records from $topic")
        Success(records)
      case Failure(e) =>
        logger.error(s"Error consuming records from $topic: ${e.getMessage}", e)
        Failure(e)
    }
  }
}
