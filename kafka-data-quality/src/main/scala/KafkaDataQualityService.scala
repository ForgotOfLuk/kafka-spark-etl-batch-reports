import com.typesafe.scalalogging.LazyLogging
import common.utils.ConfigUtils
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import utils.StreamBuilderUtils.createStreamsConfig
import utils.TopologyBuilder

object KafkaDataQualityService extends App with LazyLogging with ConfigUtils {
  private val configName = "data-quality"
  private val bootstrapServers = getBootstrapServers(configName)
  val schemaRegistryUrl = getSchemaRegistryUrl(configName)

  try {
    val builder = new StreamsBuilder()
    val topologyBuilder = new TopologyBuilder(builder, configName, schemaRegistryUrl)
    val topology = topologyBuilder.buildTopology()

    val streams = new KafkaStreams(topology, createStreamsConfig(bootstrapServers, schemaRegistryUrl))
    streams.start()
    logger.info("Kafka data quality service started")

    sys.ShutdownHookThread {
      logger.info("Shutting down Kafka data quality service")
      streams.close()
    }
  } catch {
    case e: Exception =>
      logger.error("Error in Kafka Data Quality Service: ", e)
  }
}
