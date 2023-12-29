import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.{DataFrame, SparkSession, functions}
import org.apache.spark.sql.functions.{col, collect_list, count, countDistinct, date_trunc, first, from_unixtime, struct, sum, when, window}

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
      //build the key timestamp for the mongo table from the time column that is bigInt type
      .withColumn("formattedTimestamp", from_unixtime(col("time")))
      //build watermark from the timestamp field since its already timestamp type.
      .withWatermark("timestamp", "10 minutes")
      //aggregate by the timestamp with a window of 1 minute
      .groupBy(window(col("timestamp"), "1 minute"))
      .agg(
        //get the total count of purchases
        count("*").as("purchaseCount"),
        //get the total ammount of purchase value
        sum("purchaseValue").as("totalPurchaseValue"),
        //keep the first formattedTimestamp to use as a key for the table
        first("formattedTimestamp").as("formattedTimestamp")
      )
      //prepare the userData field
      .withColumn("userData", struct(col("totalPurchaseValue"), col("purchaseCount")))
      //date_trunc to the minute for good looking information on the db
      .withColumn("timestamp", date_trunc("minute", col("formattedTimestamp")))
      //select fields to insert on the db
      .select(col("timestamp"), col("userData"))
  }
}
