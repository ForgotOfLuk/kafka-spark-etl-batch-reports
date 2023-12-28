import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, countDistinct, date_trunc, struct}

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
    val kafkaInitDF = readFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)
    val kafkaMatchDF = readFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.matchEventTopic)
    val kafkaPurchaseDF = readFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.purchaseEventTopic)

    // Transforming initial event data
    val transformedDF = transformInitEventDataFrame(kafkaInitDF)

    // Aggregate data and format for MongoDB
    val aggregatedDF = aggregateData(transformedDF)

    // Write Transformed Data to MongoDB
    writeToMongoDB(aggregatedDF, config.mongoConfig)

    // End Batch Spark session
    spark.stop()
  }

  def aggregateData(df: DataFrame): DataFrame = Try {
    logger.info("Aggregating Data")
    // Perform the aggregation
    df
      .withColumn("day", date_trunc("day", col("date"))).as("day")
      .groupBy(
        col("day"),
        col("country"),
        col("platform")
      )
      .agg(countDistinct("userId").as("numberOfUsers"))
      .withColumn("userData", struct(col("numberOfUsers"), col("country"), col("platform")))
      .select(col("day").as("timestamp"), col("userData"))
  }.getOrElse {
    logger.error("Aggregation failed")
    throw new RuntimeException("Failed to aggregate DataFrame with watermark")
  }
}
