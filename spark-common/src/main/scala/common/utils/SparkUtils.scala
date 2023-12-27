package common.utils

import com.typesafe.scalalogging.LazyLogging
import common.model.{KafkaConfig, MongoConfig}
import org.apache.spark.sql.functions.{col, from_json, to_date}
import org.apache.spark.sql.types.{LongType, StringType, StructType, TimestampType}
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

  def readFromKafkaTopic(spark: SparkSession, kafkaConfig: KafkaConfig, topic: String, startingTimestampOption: Map[String, String] = Map.empty): DataFrame = {
    spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .option("endingOffsets", "latest")
      .load()
  }

  def transformInitEventDataFrame(df: DataFrame): DataFrame = Try {
    logger.info("Transforming DataFrame")
    val schema = new StructType()
      .add("eventType", StringType, nullable = true)
      .add("time", LongType, nullable = true)
      .add("userId", StringType, nullable = true)
      .add("country", StringType, nullable = true)
      .add("platform", StringType, nullable = true)

    df.select(
        col("key").cast("string").as("_id"),
        from_json(col("value").cast("string"), schema).as("jsonData")
      )
      .select("_id", "jsonData.*")
      .withColumn("timestamp", col("time").cast(TimestampType))
      .withColumn("date", to_date(col("timestamp")))
  }.getOrElse {
    logger.error("Transformation failed")
    throw new RuntimeException("Failed to transform DataFrame")
  }

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
