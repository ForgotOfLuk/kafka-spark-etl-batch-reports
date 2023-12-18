package common

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import java.util.Properties


object KafkaAvroProducer {
  def createProducer[T](brokers: String, schemaRegistryUrl: String): KafkaProducer[String, T] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

    new KafkaProducer[String, T](props)
  }

  def sendRecord[T](producer: KafkaProducer[String, T], topic: String, key: String, record: T): Unit = {
    val producerRecord = new ProducerRecord[String, T](topic, key, record)
    producer.send(producerRecord)
  }
}