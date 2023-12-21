package utils

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.{ByteArrayKeyValueStore, StreamsBuilder}
import org.apache.kafka.streams.scala.kstream.{Consumed, Materialized}

object GlobalTableUtils {

  def createGlobalTable[K, V](builder: StreamsBuilder, topic: String, storeName: String)(implicit keySerde: Serde[K], valueSerde: Serde[V], consumedString:  Consumed[K, V]): GlobalKTable[K, V] = {

    val materialized = Materialized.as[K, V, ByteArrayKeyValueStore](storeName)
      .withKeySerde(keySerde)
      .withValueSerde(valueSerde)
    builder.globalTable(topic, materialized)
  }
}
