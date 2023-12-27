import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.functions.countDistinct
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Try

object SparkMinuteAggregatorService extends SparkUtils with LazyLogging {
  def main(args: Array[String]): Unit = {
    val config = SparkConfig.fromEnv()

    val spark = SparkSession.builder
      .appName(config.appName)
      .master(config.masterUrl)
      .config("spark.mongodb.write.connection.uri", config.mongoConfig.mongoUri)
      .getOrCreate()

    // Configure logging
    spark.sparkContext.setLogLevel("WARN")

    val kafkaDF = readStreamFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)

    val transformedDF = transformInitEventDataFrame(kafkaDF)

    val aggregatedDF = aggregateData(transformedDF).getOrElse {
      logger.error("Aggregation failed")
      throw new RuntimeException("Failed to aggregate DataFrame")
    }

    // Write Transformed Data to MongoDB
    writeStreamToMongoDB(aggregatedDF, config.mongoConfig)

    spark.streams.awaitAnyTermination()
  }

  private def aggregateData(df: DataFrame): Try[DataFrame] = Try {
    logger.info("Aggregating Data")
    df.groupBy("date", "country", "platform")
      .agg(countDistinct("userId").as("distinct_users"))
  }
}
