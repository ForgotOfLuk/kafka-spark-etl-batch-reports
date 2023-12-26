import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, expr, from_json}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{LongType, StringType, StructType}

object SparkDailyAggregatorService {
  def main(args: Array[String]): Unit = {
    val appName = sys.env.getOrElse("APP_NAME", "DefaultAppName")
    val masterUrl = sys.env.getOrElse("MASTER_URL", "local[*]")
    val kafkaBrokers = sys.env.getOrElse("KAFKA_BROKERS", "miniclip_kafka:9092")
    val topic = sys.env.getOrElse("KAFKA_INIT_TOPIC", "init_validated")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://mongodb:27017")
    val mongoCollection = sys.env.getOrElse("MONGO_COLLECTION", "mongodb://mongodb:27017")
    val mongoDb = sys.env.getOrElse("MONGO_DB", "mongodb://mongodb:27017")

    val spark = SparkSession.builder
      .appName(appName)
      .master(masterUrl)
      .config("spark.mongodb.write.connection.uri", mongoUri)
      .getOrCreate()

    // Configure logging
    spark.sparkContext.setLogLevel("WARN")

    val kafkaDF = readFromKafka(spark, kafkaBrokers, topic)

    val transformedDF = transformDataFrame(kafkaDF)

    // Print Kafka Stream to Console
    transformedDF.writeStream
      .format("console")
      .trigger(Trigger.ProcessingTime("20 seconds"))
      .start()

    // Write Transformed Data to MongoDB
    writeToMongoDB(transformedDF, mongoUri, mongoCollection, mongoDb)

    spark.streams.awaitAnyTermination()
  }

  def readFromKafka(spark: SparkSession, brokers: String, topic: String): DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()
  }

  def transformDataFrame(df: DataFrame): DataFrame = {
    // Define the schema corresponding to the JSON data
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
      .select("_id", "jsonData.*")  // Flatten the structure
  }

  def writeToMongoDB(df: DataFrame, mongoUri: String, mongoCollection: String, mongoDb: String): Unit = {
    df.writeStream
      .format("mongodb")
      .option("uri", mongoUri)
      .option("collection", mongoCollection)
      .option("database", mongoDb)
      .option("checkpointLocation", "/app")
      .outputMode("append")
      .start()
      .awaitTermination()
  }
}
