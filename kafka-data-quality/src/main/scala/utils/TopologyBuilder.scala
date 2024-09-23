package utils

import com.leca.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import com.typesafe.scalalogging.LazyLogging
import common.kafka.utils.globalktable.GlobalTableUtils.createGlobalTable
import common.kafka.utils.stream.KafkaStreamUtils.convertToStringStream
import common.utils.ConfigUtils
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import operations.StreamEnrichmentOperations.{enrichInitStream, joinInitStream, transformJoinedStream, transformStream}
import operations.join.ValidateEventsTimestamps
import operations.stream.CapitalizePlatform
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import utils.StreamBuilderUtils.{consumedString, createStream, createValueSerde, sendToTopic}
import utils.StreamProcessingUtils.{createInitEventKTable, filterKeys}
import utils.Validation.separateFunction

class TopologyBuilder(builder: StreamsBuilder, configName: String, schemaRegistryUrl: String) extends LazyLogging with ConfigUtils {

  // Method to build and return the Kafka Streams Topology
  def buildTopology(): Topology = {
    try {
      val (streamKeySerde, initEventSerde, matchEventSerde, purchaseEventSerde) = initializeSerdes(schemaRegistryUrl)

      val platformsTable = createGlobalTable[String, String](builder, getGlobalKTableTopic(configName, "platforms"), "platforms-store")
      val countriesTable = createGlobalTable[String, String](builder, getGlobalKTableTopic(configName, "countries"), "countries-store")

      logger.info("Building input streams for Kafka data quality service")
      val initEventInputStream = createStream(builder, getInputTopic(configName, "init"), streamKeySerde, initEventSerde)
      val matchEventInputStream = createStream(builder, getInputTopic(configName, "match"), streamKeySerde, matchEventSerde)
      val purchaseEventInputStream = createStream(builder, getInputTopic(configName, "purchase"), streamKeySerde, purchaseEventSerde)

      // Stream processing logic
      val (processedInitStream, initSideOutput) = enrichInitStream(initEventInputStream, platformsTable, countriesTable)

      val transformOperations = List(CapitalizePlatform)
      val transformedInitStream = transformStream(processedInitStream, transformOperations) // at this stage it goes directly to output, and to feed a new KTable

      // Create KTable from transformedInitStream
      val transformedInitTable = createInitEventKTable(transformedInitStream)

      // Join other streams with the transformedInitTable and filter based on timestamp
      val joinedMatchStream = joinInitStream(matchEventInputStream, transformedInitTable, streamKeySerde, matchEventSerde, initEventSerde)
      val joinedPurchaseStream = joinInitStream(purchaseEventInputStream, transformedInitTable, streamKeySerde, purchaseEventSerde, initEventSerde)

      val validateTimestampsOp = new ValidateEventsTimestamps[MatchEvent](_.time)
      val validatePurchaseTimestampsOp = new ValidateEventsTimestamps[InAppPurchaseEvent](_.time)

      val matchJoinOperations = List(validateTimestampsOp)
      val purchaseJoinOperations = List(validatePurchaseTimestampsOp)
      val transformedMatchStream = transformJoinedStream(joinedMatchStream, matchJoinOperations)
      val transformedPurchaseStream = transformJoinedStream(joinedPurchaseStream, purchaseJoinOperations)

      val (matchStreamOutput, matchStreamSideOutput) = filterKeys(transformedMatchStream, separateFunction)
      val (purchaseStreamOutput, purchaseStreamSideOutput) = filterKeys(transformedPurchaseStream, separateFunction)

      // Send joined streams to respective output topics
      logger.info("Building output streams for Kafka data quality service")
      sendToTopics(streamKeySerde, initEventSerde, matchEventSerde, purchaseEventSerde, initSideOutput, transformedInitStream, matchStreamOutput, matchStreamSideOutput, purchaseStreamOutput, purchaseStreamSideOutput)

      builder.build()
    } catch {
      case e: Exception =>
        logger.error("Error building the Kafka Streams topology", e)
        throw e // Rethrowing the exception for upstream handling
    }
  }

  private def sendToTopics(streamKeySerde: Serde[String], initEventSerde: SpecificAvroSerde[InitEvent], matchEventSerde: SpecificAvroSerde[MatchEvent], purchaseEventSerde: SpecificAvroSerde[InAppPurchaseEvent], initSideOutput: KStream[String, InitEvent], transformedInitStream: KStream[String, InitEvent], matchStreamOutput: KStream[String, MatchEvent], matchStreamSideOutput: KStream[String, MatchEvent], purchaseStreamOutput: KStream[String, InAppPurchaseEvent], purchaseStreamSideOutput: KStream[String, InAppPurchaseEvent]): Unit = {
    sendToTopic(transformedInitStream.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "init"), streamKeySerde, initEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
    sendToTopic(convertToStringStream(transformedInitStream), getOutputTopic(configName, "init_enriched"), streamKeySerde, streamKeySerde)
    sendToTopic(matchStreamOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "match"), streamKeySerde, matchEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
    sendToTopic(purchaseStreamOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "purchase"), streamKeySerde, purchaseEventSerde.asInstanceOf[Serde[SpecificRecordBase]])

    //persist all data
    sendToTopic(initSideOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "init_side_output"), streamKeySerde, initEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
    sendToTopic(matchStreamSideOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "match_side_output"), streamKeySerde, matchEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
    sendToTopic(purchaseStreamSideOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "purchase_side_output"), streamKeySerde, purchaseEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
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
