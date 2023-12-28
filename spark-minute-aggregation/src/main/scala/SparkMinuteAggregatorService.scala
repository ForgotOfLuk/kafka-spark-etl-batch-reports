import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.{DataFrame, SparkSession, functions}
import org.apache.spark.sql.functions.{col, count, countDistinct, date_trunc, struct, when, window}

import scala.util.Try

object SparkMinuteAggregatorService extends SparkUtils with LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info("Starting SparkDailyAggregatorService...")
    val config = SparkConfig.fromEnv()

    // Initialize Spark Session
    val spark = SparkSession.builder
      .appName(config.appName)
      .master(config.masterUrl)
      .config("spark.mongodb.write.connection.uri", config.mongoConfig.mongoUri)
      .getOrCreate()

    // Read from Kafka topic and add watermark
    val kafkaInitDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)
      .withWatermark("timestamp", "2 minutes")
    val kafkaMatchDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.matchEventTopic)
      .withWatermark("timestamp", "2 minutes")
    val kafkaPurchaseDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)
      .withWatermark("timestamp", "2 minutes")

    // Transforming initial event data
    val transformedInitDF = transformInitEventDataFrame(kafkaInitDF)
    val transformedMatchDF = transformMatchEventDataFrame(kafkaMatchDF)
    val transformedPurchaseDF = transformPurchaseEventDataFrame(kafkaPurchaseDF)


    // Aggregating data
    val aggregatedDF = aggregateData(transformedPurchaseDF, transformedMatchDF, transformedInitDF)

    // End Batch Spark session
    writeStreamToMongoDB(aggregatedDF, config.mongoConfig)

    spark.streams.awaitAnyTermination()
  }

  def aggregateData(purchaseDF: DataFrame, matchDF: DataFrame, initDF: DataFrame): DataFrame = Try {
    logger.info("Aggregating Data")
    // Join the DataFrames
    val joinedDF = purchaseDF
      .join(matchDF, Seq("userId"), "left")
      .join(initDF, Seq("userId"), "left")
      .withWatermark("timestamp", "2 minutes")

    // Aggregate data for total counts and sums
    val totalAggregates = joinedDF
      .groupBy(window(col("timestamp"), "1 minute"))
      .agg(
        count("purchase_value").as("totalPurchaseCount"),
        functions.sum("purchase_value").as("totalRevenue"),
        countDistinct("userId").as("totalDistinctUsers")
      )
      .withWatermark("timestamp", "2 minutes")

    // Revenue by country and matches by country
    val countryAggregates = joinedDF
      .groupBy(window(col("timestamp"), "1 minute"), col("country"))
      .agg(
        functions.sum("purchase_value").as("revenueByCountry"),
        count(when(col("eventType") === "match", 1)).as("matchesByCountry")
      )
      .withWatermark("timestamp", "2 minutes")

    // Joining the aggregated results
    totalAggregates.join(countryAggregates, "window")
      .withWatermark("timestamp", "2 minutes")
  }.getOrElse {
    logger.error("Aggregation failed")
    throw new RuntimeException("Failed to aggregate DataFrame with watermark")
  }
}
