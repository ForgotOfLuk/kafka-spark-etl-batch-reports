import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.{DataFrame, SparkSession, functions}
import org.apache.spark.sql.functions.{col, collect_list, count, countDistinct, date_trunc, first, struct, sum, when, window}

import scala.util.Try

object SparkMinuteAggregatorService extends SparkUtils with LazyLogging {
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

    // Read from Kafka topic and add watermark
    val kafkaPurchaseDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)

    // Transforming initial event data
    val transformedPurchaseDF = transformPurchaseEventDataFrame(kafkaPurchaseDF)

    // Aggregating data
    val aggregatedDF = aggregatePurchaseDF(transformedPurchaseDF)

    // End Batch Spark session
    writeStreamToMongoDB(aggregatedDF, config.mongoConfig)

    spark.streams.awaitAnyTermination()
  }

  // Modified aggregatePurchaseDF function
  def aggregatePurchaseDF(purchaseDF: DataFrame): DataFrame = {
    purchaseDF
      .withWatermark("timestamp", "10 minutes")
      .groupBy(window(col("timestamp"), "1 minute"))
      .agg(
        count("*").as("purchaseCount"),
        sum("purchaseValue").as("totalPurchaseValue"),
        first("date").as("date")
      )
      .withColumn("userData", struct(col("totalPurchaseValue"), col("purchaseCount")))
      .withColumn("minute", date_trunc("minute", col("date")))
      .select(col("minute").as("timestamp"), col("userData"))
  }
}
