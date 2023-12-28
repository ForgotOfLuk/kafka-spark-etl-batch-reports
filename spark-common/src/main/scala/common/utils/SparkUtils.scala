package common.utils

import com.typesafe.scalalogging.LazyLogging
import common.model.data.SparkSchemas
import common.model.{KafkaConfig, MongoConfig}
import org.apache.spark.sql.functions.{col, from_json, to_date}
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType, StructType, TimestampType}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Try

trait SparkUtils extends LazyLogging{
  def readStreamFromKafkaTopic(spark: SparkSession, kafkaConfig: KafkaConfig, topic: String): DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()
  }

  def readFromKafkaTopic(spark: SparkSession, kafkaConfig: KafkaConfig, topic: String): DataFrame = {
    spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.kafkaBrokers)
      .option("subscribe", topic)
      .option(kafkaConfig.offsetConfigKey, kafkaConfig.offsetConfigValue)
      .option("endingOffsets", "latest")
      .load()
  }
  /**
   * Transforms the DataFrame for generic events based on the provided schema.
   * @param df The DataFrame containing the raw Kafka messages.
   * @param schema The schema of the raw Kafka messages.
   * @param eventType The name of the event.
   * @return Transformed DataFrame with structured columns.
   */
  private def transformDataFrame(df: DataFrame, schema: StructType, eventType: String): DataFrame = Try {
    logger.info(s"Transforming DataFrame for $eventType events")
    df.select(
        col("key").cast("string").as("_id"),
        from_json(col("value").cast("string"), schema).as("jsonData")
      )
      .select("_id", "jsonData.*")
      .withColumn("timestamp", (col("time") / 1000).cast(TimestampType))
      .withColumn("date", to_date(col("timestamp")))
  }.getOrElse {
    logger.error(s"Transformation failed for $eventType events")
    throw new RuntimeException(s"Failed to transform DataFrame for $eventType events")
  }

  def transformInitEventDataFrame(df: DataFrame): DataFrame = transformDataFrame(df, SparkSchemas.initEventSchema, "initEvent")

  def transformPurchaseEventDataFrame(df: DataFrame): DataFrame = transformDataFrame(df, SparkSchemas.purchaseEventSchema, "purchaseEvent")

  def transformMatchEventDataFrame(df: DataFrame): DataFrame = transformDataFrame(df, SparkSchemas.matchEventSchema, "matchEvent")

  def writeStreamToMongoDB(df: DataFrame, mongoConfig: MongoConfig): Unit = {
    df.writeStream
      .format("mongodb")
      .option("uri", mongoConfig.mongoUri)
      .option("collection", mongoConfig.mongoCollection)
      .option("database", mongoConfig.mongoDb)
      .option("checkpointLocation", "/app")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def writeToMongoDB(df: DataFrame, mongoConfig: MongoConfig): Unit = {
    df.write
      .format("mongodb")
      .mode("append")
      .option("uri", mongoConfig.mongoUri)
      .option("collection", mongoConfig.mongoCollection)
      .option("database", mongoConfig.mongoDb)
      .option("checkpointLocation", "/app")
      .save()
  }
}
