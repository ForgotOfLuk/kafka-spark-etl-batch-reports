import aggregation.AggregationFunctions.{aggregateEnrichedPurchaseDF, aggregatePurchaseDF}
import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
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
    val spark = initializeSparkSession(config)

    logger.info("Spark Session initialized.")

    // Read data from Kafka topic for purchase events.
    val kafkaPurchaseDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)

    logger.info("Data read from Kafka topics for initial and purchase events.")

    // Transform initial event data to the correct schema.
    val transformedPurchaseDF = transformPurchaseEventDataFrame(kafkaPurchaseDF)

    // data processing
    val processDataResult = processData(
      transformedPurchaseDF,
      config,
      aggregatePurchaseDF,
      aggregateEnrichedPurchaseDF
    )

    //writing processed data
    val sendDataResult = processDataResult.flatMap { aggregatedDF =>
      sendData(
        aggregatedDF,
        config,
        writeStreamToMongoDB(_, config.mongoConfig, _)
      )
    }

    sendDataResult match {
      case Success(streamingQuery) =>
        logger.info("Data processed and written to MongoDB successfully, awaiting termination...")
        streamingQuery.awaitTermination()
      case Failure(exception) =>
        logger.error("An error occurred in the data processing pipeline", exception)
    }
    // Await termination of the streaming queries.
    spark.streams.awaitAnyTermination()
    logger.info("SparkMinutePurchaseAggregatorService terminated.")
  }
}
