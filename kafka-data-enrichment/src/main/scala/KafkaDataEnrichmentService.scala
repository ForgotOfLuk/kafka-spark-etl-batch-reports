import com.typesafe.scalalogging.LazyLogging
import common.utils.ConfigUtils
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import utils.StreamBuilderUtils.createStreamsConfig
import utils.TopologyBuilder

import scala.util.{Failure, Success, Try}

class KafkaDataEnrichmentService(bootstrapServers: String, schemaRegistryUrl: String) extends LazyLogging {
  def start(): Unit = {
    Try {
      val builder = new StreamsBuilder()
      val topologyBuilder = new TopologyBuilder(builder, "data-enrichment", schemaRegistryUrl)
      val topology = topologyBuilder.buildTopology()

      val streams = new KafkaStreams(topology, createStreamsConfig(bootstrapServers, schemaRegistryUrl))
      streams.start()
      sys.ShutdownHookThread {
        streams.close()
      }

      streams
    } match {
      case Success(_) =>
        logger.info("Kafka data quality service started")
      case Failure(e) =>
        logger.error("Error in Kafka Data Quality Service: ", e)
    }
  }
}
