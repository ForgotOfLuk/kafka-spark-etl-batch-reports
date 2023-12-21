package utils

import com.typesafe.scalalogging.LazyLogging
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

import java.util.{Collections, Properties}

object StreamBuilderUtils extends LazyLogging {

  // Creating Global Tables
  implicit val consumedString: Consumed[String, String] = Consumed.`with`[String, String](stringSerde, stringSerde)

  def createValueSerde[T <: SpecificRecordBase](schemaRegistryUrl: String): SpecificAvroSerde[T] = {
    val serde = new SpecificAvroSerde[T]
    serde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false)
    serde
  }

  def createStream[K, V](builder: StreamsBuilder, sourceTopic: String, keySerde: Serde[K], valueSerde: Serde[V]): KStream[K, V] = {
    logger.info(s"Creating stream from $sourceTopic")
    builder.stream[K, V](sourceTopic)(Consumed.`with`(keySerde, valueSerde))
  }

  def sendToTopic[K, V <: SpecificRecordBase](stream: KStream[K, V], destTopic: String, keySerde: Serde[K], valueSerde: Serde[V]): Unit = {
    logger.info(s"Sending stream to $destTopic")
    stream.to(destTopic)(Produced.`with`(keySerde, valueSerde))
  }

  def createStreamsConfig(bootstrapServers: String, schemaRegistryUrl: String): Properties = {
    val config = new Properties()
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-data-quality-service")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

    // Set the default key serde to String
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass.getName)

    // Set the default value serde to SpecificAvroSerde
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[SpecificAvroSerde[_]].getName)

    // Additionally, set this property for schema registry
    val serdeConfig = Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    val valueSerde = new SpecificAvroSerde[SpecificRecordBase]()
    valueSerde.configure(serdeConfig, false) // `false` for value SerDe

    config
  }

}
