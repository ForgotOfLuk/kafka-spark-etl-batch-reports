import aggregation.AggregationFunctions.{aggregateEnrichedMatchDF, getUsersByTimeDF}
import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.SparkSession

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
    val spark = SparkSession.builder
      .appName(config.appName)
      .master(config.masterUrl)
      .config("spark.mongodb.write.connection.uri", config.mongoConfig.mongoUri)
      .config("checkpointLocation", "/app/data/checkpoint")
      .getOrCreate()

    logger.info("Spark Session initialized.")

    // Read data from Kafka topics.
    val kafkaMatchDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.matchEventTopic)

    logger.info("Data read from Kafka topics.")

    // Transform initial event data with the correct schemas.
    val transformedMatchDF = transformMatchEventDataFrame(kafkaMatchDF)

    // Chain data processing steps using for-comprehension for linear and readable operations.
    val result = for {
      // Aggregate enriched match data for analytical purposes.
      aggregatedEnrichedMatchDF <- aggregateEnrichedMatchDF(transformedMatchDF)

      // Aggregate match data to get distinct user activity per minute.
      aggregatedMatchDF <- getUsersByTimeDF(transformedMatchDF)
    } yield (aggregatedMatchDF, aggregatedEnrichedMatchDF)

    // Handle the outcome of the data processing.
    result match {
      case Success((aggregatedMatchDF, aggregatedEnrichedMatchDF)) =>
        // Write the resulting DataFrames to MongoDB.
        writeStreamToMongoDB(aggregatedMatchDF, config.mongoConfig, config.mongoConfig.mongoCollection)
        writeStreamToMongoDB(aggregatedEnrichedMatchDF, config.mongoConfig, config.mongoConfig.mongoEnrichedCollection)
        logger.info("Aggregated data written to MongoDB.")

      case Failure(exception) =>
        // Log any errors encountered during the data processing pipeline.
        logger.error("An error occurred in the data processing pipeline", exception)
    }

    // Await termination of the streaming queries.
    spark.streams.awaitAnyTermination()
    logger.info("SparkMinuteMatchAggregatorService terminated.")
  }
}
