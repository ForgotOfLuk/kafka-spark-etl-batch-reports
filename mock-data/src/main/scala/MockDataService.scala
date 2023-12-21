import com.typesafe.scalalogging.LazyLogging
import utils.Utils.{generateData, loadConfig, setupKafkaProducers}

import scala.util.{Failure, Success}

/**
 * Main class for MockDataService.
 * Initializes the service by loading configuration, setting up Kafka producers, and starting data generation.
 */
object MockDataService extends LazyLogging {

  def main(args: Array[String]): Unit = {
    logger.info("Initializing MockDataService")

    // Load configurations
    loadConfig() match {
      case Success((kafkaConfig, mockConfig)) =>
        logger.info("Configurations loaded successfully")

        // Setup Kafka producers
        val kafkaProducers = setupKafkaProducers(kafkaConfig)
        logger.info("Kafka producers set up")

        // Start data generation process
        generateData(kafkaProducers, kafkaConfig, mockConfig)
        logger.info("Data generation started")

      case Failure(exception) =>
        logger.error("Failed to initialize MockDataService", exception)
    }
  }
}