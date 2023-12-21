package utils

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.{ByteArrayKeyValueStore, StreamsBuilder}
import org.apache.kafka.streams.scala.kstream.{Consumed, Materialized}
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Try, Success, Failure}

object GlobalTableUtils extends LazyLogging {

  // Method to create a GlobalKTable from a given topic
  def createGlobalTable[K, V](builder: StreamsBuilder, topic: String, storeName: String)
                             (implicit keySerde: Serde[K], valueSerde: Serde[V], consumedString: Consumed[K, V]): GlobalKTable[K, V] = {
    Try {
      val materialized = Materialized.as[K, V, ByteArrayKeyValueStore](storeName)
        .withKeySerde(keySerde)
        .withValueSerde(valueSerde)

      // Building the global table from the specified topic
      builder.globalTable(topic, materialized)
    } match {
      case Success(globalTable) =>
        logger.info(s"GlobalKTable created for topic $topic with store name $storeName")
        globalTable
      case Failure(exception) =>
        logger.error(s"Error creating GlobalKTable for topic $topic: ${exception.getMessage}")
        throw exception // Rethrow the exception to handle it upstream if necessary
    }
  }
}
