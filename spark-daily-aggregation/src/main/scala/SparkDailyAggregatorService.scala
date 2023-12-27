import com.typesafe.scalalogging.LazyLogging
import common.model.SparkConfig
import common.utils.SparkUtils
import org.apache.spark.sql.SparkSession
import utils.PipelineUtils.aggregateData

object SparkDailyAggregatorService extends SparkUtils with LazyLogging {
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
    val kafkaDF = readFromKafkaTopic(spark, config.kafkaConfig, config.kafkaConfig.initEventTopic)

    // Transforming initial event data
    val transformedDF = transformInitEventDataFrame(kafkaDF)

    // Aggregate data and format for MongoDB
    val aggregatedDF = aggregateData(transformedDF)

    // Write Transformed Data to MongoDB
    writeToMongoDB(aggregatedDF, config.mongoConfig)

    // End Batch Spark session
    spark.stop()
  }

}
