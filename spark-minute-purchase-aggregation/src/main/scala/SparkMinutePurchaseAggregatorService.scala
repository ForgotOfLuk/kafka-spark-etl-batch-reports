import aggregation.AggregationFunctions.{aggregateEnrichedPurchaseDF, aggregatePurchaseDF}
import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import enrich.EnrichFunctions.enrichPurchaseDF
import org.apache.spark.sql.SparkSession

import scala.util.{Failure, Success}

object SparkMinutePurchaseAggregatorService extends SparkUtils with LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info("Starting SparkMinuteAggregatorService...")
    val config = SparkConfig.fromEnv()

    // Initialize Spark Session
    val spark = SparkSession.builder
      .appName(config.appName)
      .master(config.masterUrl)
      .config("spark.mongodb.write.connection.uri", config.mongoConfig.mongoUri)
      .config("checkpointLocation", "/app/data/checkpoint")
      .getOrCreate()

    // Read from Kafka topic
    val kafkaInitDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)
    val kafkaPurchaseDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)

    // Transforming initial event data with the correct schemas
    val transformedInitDF = transformInitEventDataFrame(kafkaInitDF)
    val transformedPurchaseDF = transformPurchaseEventDataFrame(kafkaPurchaseDF)

    // Utilize a for-comprehension to chain the data processing steps. This approach allows
    // a linear and readable flow of operations, handling each step's success or failure.
    val result = for {
      // Aggregate purchase data. This step summarizes the purchase data into a more
      // compact form suitable for further processing.
      aggregatedPurchaseDF <- aggregatePurchaseDF(transformedPurchaseDF)

      // Enrich the purchase data with user information. This step combines the purchase
      // data with additional user details, enhancing the dataset with more attributes.
      enrichedPurchaseDF <- enrichPurchaseDF(transformedInitDF, transformedPurchaseDF)

      // Aggregate the enriched purchase data. This step further summarizes the enriched
      // purchase data, often used for analytics and reporting.
      aggregatedEnrichedPurchaseDF <- aggregateEnrichedPurchaseDF(enrichedPurchaseDF)

    } yield (aggregatedPurchaseDF, aggregatedEnrichedPurchaseDF)

    // Handle the outcome of the for-comprehension.
    result match {
      case Success((aggregatedPurchaseDF, aggregatedEnrichedPurchaseDF)) =>
        // In case of success, write the resulting DataFrame to MongoDB.
        writeStreamToMongoDB(aggregatedPurchaseDF, config.mongoConfig)
        aggregatedEnrichedPurchaseDF.printSchema() //TODO understand if i want to add a new job for country aggregations or not

      case Failure(exception) =>
        // In case of failure, log the error. This is crucial for diagnosing issues
        // in the data processing pipeline.
        logger.error("An error occurred in the data processing pipeline", exception)
    }


    // Await termination
    spark.streams.awaitAnyTermination()
    logger.info("SparkMinuteAggregatorService terminated.")
  }
}
