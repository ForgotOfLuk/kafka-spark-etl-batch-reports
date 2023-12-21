import com.miniclip.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import com.typesafe.scalalogging.LazyLogging
import common.utils.ConfigUtils
import operations.StreamEnrichmentOperations.{enrichInitStream, joinInitStream, transformJoinedStream, transformStream}
import operations.stream.{CapitalizePlatform, JoinedStreamOperation, ValidateEventsTimestamps}
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import utils.GlobalTableUtils.createGlobalTable
import utils.StreamBuilderUtils.{consumedString, createStream, createStreamsConfig, createValueSerde, sendToTopic}
import utils.StreamProcessingUtils.{createInitEventKTable, getKeyBranches, getValueBranches, separateFunction}

object KafkaDataQualityService extends App with LazyLogging with ConfigUtils {
  private val configName = "data-quality"
  private val bootstrapServers = getBootstrapServers(configName)
  val schemaRegistryUrl = getSchemaRegistryUrl(configName)

  val builder = new StreamsBuilder()
  // Serdes
  private val streamKeySerde = Serdes.stringSerde
  private val initEventSerde = createValueSerde[InitEvent](schemaRegistryUrl)
  private val matchEventSerde = createValueSerde[MatchEvent](schemaRegistryUrl)
  private val purchaseEventSerde = createValueSerde[InAppPurchaseEvent](schemaRegistryUrl)

  val platformsTable = createGlobalTable[String, String](builder, getGlobalKTableTopic(configName, "platforms"), "platforms-store")
  val countriesTable = createGlobalTable[String, String](builder, getGlobalKTableTopic(configName, "countries"), "countries-store")

  logger.info("Building input streams for Kafka data quality service")
  val initEventInputStream = createStream(builder, getInputTopic(configName, "init"), streamKeySerde, initEventSerde)
  val matchEventInputStream = createStream(builder, getInputTopic(configName, "match"), streamKeySerde, matchEventSerde)
  val purchaseEventInputStream = createStream(builder, getInputTopic(configName, "purchase"), streamKeySerde, purchaseEventSerde)

  // Stream processing logic
  private val (processedInitStream, initSideOutput) = enrichInitStream(initEventInputStream, platformsTable, countriesTable)

  val transformOperations = List(CapitalizePlatform)
  private val transformedInitStream = transformStream(processedInitStream, transformOperations) // at this stage it goes directly to output, and to feed a new KTable

  // Create KTable from transformedInitStream
  val transformedInitTable = createInitEventKTable(transformedInitStream)

  // Join other streams with the transformedInitTable and filter based on timestamp
  private val joinedMatchStream = joinInitStream(matchEventInputStream, transformedInitTable, streamKeySerde, matchEventSerde, initEventSerde)
  private val joinedPurchaseStream = joinInitStream(purchaseEventInputStream, transformedInitTable, streamKeySerde, purchaseEventSerde, initEventSerde)

  private val validateTimestampsOp = new ValidateEventsTimestamps[MatchEvent](_.time)
  private val validatePurchaseTimestampsOp = new ValidateEventsTimestamps[InAppPurchaseEvent](_.time)

  private val matchJoinOperations = List(validateTimestampsOp)
  private val purchaseJoinOperations = List(validatePurchaseTimestampsOp)
  private val transformedMatchStream = transformJoinedStream(joinedMatchStream, matchJoinOperations)
  private val transformedPurchaseStream = transformJoinedStream(joinedPurchaseStream, purchaseJoinOperations)

  private val (matchStreamOutput, matchStreamSideOutput) = getKeyBranches("match", transformedMatchStream, separateFunction)
  private val (purchaseStreamOutput, purchaseStreamSideOutput) = getKeyBranches("purchase", transformedPurchaseStream, separateFunction)

  // Send joined streams to respective output topics
  logger.info("Building output streams for Kafka data quality service")
  sendToTopic(transformedInitStream.asInstanceOf[KStream[String ,SpecificRecordBase]], getOutputTopic(configName, "init"), streamKeySerde, initEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
  sendToTopic(matchStreamOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "match"), streamKeySerde, matchEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
  sendToTopic(purchaseStreamOutput.asInstanceOf[KStream[String, SpecificRecordBase]], getOutputTopic(configName, "purchase"), streamKeySerde, matchEventSerde.asInstanceOf[Serde[SpecificRecordBase]])

  //persist all data
  sendToTopic(initSideOutput.asInstanceOf[KStream[String ,SpecificRecordBase]], getOutputTopic(configName, "init_side_output"), streamKeySerde, initEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
  sendToTopic(matchStreamSideOutput.asInstanceOf[KStream[String ,SpecificRecordBase]], getOutputTopic(configName, "match_side_output"), streamKeySerde, matchEventSerde.asInstanceOf[Serde[SpecificRecordBase]])
  sendToTopic(purchaseStreamSideOutput.asInstanceOf[KStream[String ,SpecificRecordBase]], getOutputTopic(configName, "purchase_side_output"), streamKeySerde, purchaseEventSerde.asInstanceOf[Serde[SpecificRecordBase]])

  val streams = new KafkaStreams(builder.build(), createStreamsConfig(bootstrapServers, schemaRegistryUrl))
  streams.start()

  logger.info("Kafka data quality service started")

  sys.ShutdownHookThread {
    logger.info("Shutting down Kafka data quality service")
    streams.close()
  }
}
