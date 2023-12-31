import aggregation.AggregationFunctions.{aggregateEnrichedMatchDF, getUsersByTimeDF}
import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils

import scala.util.{Failure, Success}

/**
 * Service class for aggregating match data on a per-minute basis.
 * This service reads match and user data, enriches and aggregates the data,
 * and writes the results to MongoDB.
 */
object SparkMinuteMatchAggregatorService extends SparkUtils with LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info("Starting SparkMinuteMatchAggregatorService...")

    // Load configuration settings.
    val config = SparkConfig.fromEnv()

    // Initialize Spark Session.
    val spark = initializeSparkSession(config)

    logger.info("Spark Session initialized.")

    // Read data from Kafka topics.
    val kafkaMatchDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.matchEventTopic)

    logger.info("Data read from Kafka topics.")

    // Transform initial event data with the correct schemas.
    val transformedMatchDF = transformMatchEventDataFrame(kafkaMatchDF)

    // Chain data processing/writing steps using for-comprehension.
    val result = for {
      aggregatedDF <- processData(
        transformedMatchDF,
        config,
        getUsersByTimeDF,
        aggregateEnrichedMatchDF
      )
      streamingQuery <- sendData(
        aggregatedDF,
        config,
        writeStreamToMongoDB(_, config.mongoConfig, _)
      )
    } yield streamingQuery

    result match {
      case Success(streamingQuery) =>
        logger.info("Data processing and sending started, awaiting termination...")
        streamingQuery.awaitTermination()
      case Failure(exception) =>
        logger.error("An error occurred in the data processing pipeline", exception)
    }

    // Await termination of the streaming queries.
    spark.streams.awaitAnyTermination()
    logger.info("SparkMinuteMatchAggregatorService terminated.")
  }
}
