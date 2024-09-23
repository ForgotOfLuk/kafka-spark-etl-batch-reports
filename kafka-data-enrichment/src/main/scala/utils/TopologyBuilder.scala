package utils

import com.leca.avro._
import com.typesafe.scalalogging.LazyLogging
import common.kafka.utils.globalktable.GlobalTableUtils.createGlobalTable
import common.kafka.utils.stream.KafkaStreamUtils.convertToStringStream
import common.utils.ConfigUtils
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import operations.StreamEnrichmentOperations.{enrichInAppPurchaseStream, enrichMatchStream}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream}
import org.apache.kafka.streams.scala.serialization.Serdes
import utils.StreamBuilderUtils.{createStream, createValueSerde, sendToTopic}

class TopologyBuilder(builder: StreamsBuilder, configName: String, schemaRegistryUrl: String) extends LazyLogging with ConfigUtils {

  // Method to build and return the Kafka Streams Topology
  def buildTopology(): Topology = {
    try {
      val (streamKeySerde, initEventSerde, matchEventSerde, purchaseEventSerde) = initializeSerdes(schemaRegistryUrl)
      val initTable = createGlobalTable[String, InitEvent](builder, getGlobalKTableTopic(configName, "init"), "init-global-store")(streamKeySerde, initEventSerde, Consumed.`with`(streamKeySerde, initEventSerde))

      logger.info("Building input streams for Kafka data quality service")
      val matchEventInputStream = createStream(builder, getInputTopic(configName, "match"), streamKeySerde, matchEventSerde)
      val purchaseEventInputStream = createStream(builder, getInputTopic(configName, "purchase"), streamKeySerde, purchaseEventSerde)

      // Enrich logic
      val (enrichedMatchStream, enrichedMatchStreamSideOutput) = enrichMatchStream(matchEventInputStream, initTable)
      val (enrichedPurchaseStream, enrichedPurchaseStreamSideOutput) = enrichInAppPurchaseStream(purchaseEventInputStream, initTable)

      // Send joined streams to respective output topics
      logger.info("Building output streams for Kafka data enrichment service")
      sendToTopics(streamKeySerde, matchEventSerde, purchaseEventSerde, enrichedMatchStream, enrichedMatchStreamSideOutput, enrichedPurchaseStream, enrichedPurchaseStreamSideOutput)

      builder.build()
    } catch {
      case e: Exception =>
        logger.error("Error building the Kafka Streams topology", e)
        throw e // Rethrowing the exception for upstream handling
    }
  }

  private def sendToTopics(
    streamKeySerde: Serde[String],
    matchEventSerde: SpecificAvroSerde[MatchEvent],
    purchaseEventSerde: SpecificAvroSerde[InAppPurchaseEvent],
    enrichedMatchStream: KStream[String, EnrichedMatchEvent],
    matchStreamSideOutput: KStream[String, MatchEvent],
    enrichedPurchaseStream: KStream[String, EnrichedInAppPurchaseEvent],
    purchaseStreamSideOutput: KStream[String, InAppPurchaseEvent]
  ): Unit = {
    //side outputs could possibly enter a scheduler for re-ingestion
    sendToTopic(convertToStringStream(enrichedMatchStream), getOutputTopic(configName, "match"), streamKeySerde, streamKeySerde)
    sendToTopic(matchStreamSideOutput, getOutputTopic(configName, "match_side_output"), streamKeySerde, matchEventSerde)
    sendToTopic(convertToStringStream(enrichedPurchaseStream), getOutputTopic(configName, "purchase"), streamKeySerde, streamKeySerde)
    sendToTopic(purchaseStreamSideOutput, getOutputTopic(configName, "purchase_side_output"), streamKeySerde, purchaseEventSerde)
  }
  private def initializeSerdes(schemaRegistryUrl: String): (Serde[String], SpecificAvroSerde[InitEvent], SpecificAvroSerde[MatchEvent], SpecificAvroSerde[InAppPurchaseEvent]) = {
    // Serdes
    val streamKeySerde = Serdes.stringSerde
    val initEventSerde = createValueSerde[InitEvent](schemaRegistryUrl)
    val matchEventSerde = createValueSerde[MatchEvent](schemaRegistryUrl)
    val purchaseEventSerde = createValueSerde[InAppPurchaseEvent](schemaRegistryUrl)

    (streamKeySerde, initEventSerde, matchEventSerde, purchaseEventSerde)
  }
}
