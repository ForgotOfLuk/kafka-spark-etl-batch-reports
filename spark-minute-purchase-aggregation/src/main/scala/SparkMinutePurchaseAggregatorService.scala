import aggregation.AggregationFunctions.{aggregateEnrichedPurchaseDF, aggregatePurchaseDF}
import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import enrich.EnrichFunctions.enrichPurchaseDF
import org.apache.spark.sql.SparkSession

import scala.util.{Failure, Success}

/**
 * Service class for aggregating purchase data.
 * This service reads purchase data and initial user data, performs enrichment and aggregation,
 * and writes the results to MongoDB.
 */
object SparkMinutePurchaseAggregatorService extends SparkUtils with LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info("Starting SparkMinutePurchaseAggregatorService...")

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

    // Read data from Kafka topics for initial and purchase events.
    val kafkaInitDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)
    val kafkaPurchaseDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)

    logger.info("Data read from Kafka topics for initial and purchase events.")

    // Transform initial event data to the correct schema.
    val transformedInitDF = transformInitEventDataFrame(kafkaInitDF)
    val transformedPurchaseDF = transformPurchaseEventDataFrame(kafkaPurchaseDF)

    // Chain data processing steps using for-comprehension for linear and readable operations.
    val result = for {
      // Aggregate purchase data for further processing.
      aggregatedPurchaseDF <- aggregatePurchaseDF(transformedPurchaseDF)

      // Enrich purchase data with additional user details.
      enrichedPurchaseDF <- enrichPurchaseDF(transformedInitDF, transformedPurchaseDF)

      // Aggregate the enriched purchase data for analytics and reporting.
      aggregatedEnrichedPurchaseDF <- aggregateEnrichedPurchaseDF(enrichedPurchaseDF)
    } yield (aggregatedPurchaseDF, aggregatedEnrichedPurchaseDF)

    // Handle the outcome of the data processing.
    result match {
      case Success((aggregatedPurchaseDF, aggregatedEnrichedPurchaseDF)) =>
        // Write the resulting DataFrames to MongoDB.
        writeStreamToMongoDB(aggregatedPurchaseDF, config.mongoConfig, config.mongoConfig.mongoCollection)
        writeStreamToMongoDB(aggregatedEnrichedPurchaseDF, config.mongoConfig, config.mongoConfig.mongoEnrichedCollection)
        logger.info("Aggregated data written to MongoDB.")

      case Failure(exception) =>
        // Log any errors encountered during the data processing pipeline.
        logger.error("An error occurred in the data processing pipeline", exception)
    }

    // Await termination of the streaming queries.
    spark.streams.awaitAnyTermination()
    logger.info("SparkMinutePurchaseAggregatorService terminated.")
  }
}
