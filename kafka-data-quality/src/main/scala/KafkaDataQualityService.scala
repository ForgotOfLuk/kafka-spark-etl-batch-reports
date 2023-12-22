import com.typesafe.scalalogging.LazyLogging
import common.utils.ConfigUtils
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import utils.StreamBuilderUtils.createStreamsConfig
import utils.TopologyBuilder

import scala.util.{Failure, Success, Try}

class KafkaDataQualityService(bootstrapServers: String, schemaRegistryUrl: String) extends LazyLogging {
  def start(): Unit = {
    Try {
      val builder = new StreamsBuilder()
      val topologyBuilder = new TopologyBuilder(builder, "data-quality", schemaRegistryUrl)
      val topology = topologyBuilder.buildTopology()

      val streams = new KafkaStreams(topology, createStreamsConfig(bootstrapServers, schemaRegistryUrl))
      streams.start()
      sys.ShutdownHookThread {
        streams.close()
      }

      streams
    } match {
      case Success(streams) =>
        logger.info("Kafka data quality service started")
      // Additional code to manage the streams, if needed
      case Failure(e) =>
        logger.error("Error in Kafka Data Quality Service: ", e)
    }
  }
}

object KafkaDataQualityServiceApp extends App with ConfigUtils {
  private val configName = "data-quality"
  private val bootstrapServers = getBootstrapServers(configName)
  private val schemaRegistryUrl = getSchemaRegistryUrl(configName)

  val service = new KafkaDataQualityService(bootstrapServers, schemaRegistryUrl)
  service.start()
}
