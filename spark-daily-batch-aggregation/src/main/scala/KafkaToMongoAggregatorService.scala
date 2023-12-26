import org.apache.avro.Schema
import org.apache.spark.sql.avro.functions.from_avro
import org.apache.spark.sql.functions.{col, lit, rand}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.File

object KafkaToMongoAggregatorService {
  def main(args: Array[String]): Unit = {
    val appName = sys.env.getOrElse("APP_NAME", "DefaultAppName")
    val masterUrl = sys.env.getOrElse("MASTER_URL", "local[*]")
    val kafkaBrokers = sys.env.getOrElse("KAFKA_BROKERS", "miniclip_kafka:9092")
    val topic = sys.env.getOrElse("KAFKA_INIT_TOPIC", "init_validated")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://mongodb:27017")

    val spark = SparkSession.builder
      .appName(appName)
      .master(masterUrl)
      .config("spark.mongodb.write.connection.uri", mongoUri)
      .getOrCreate()

    // Configure logging
    spark.sparkContext.setLogLevel("WARN")

    val avroSchema = new Schema.Parser().parse(new File("/app/schemas/init.avsc"))
    val df = readFromKafka(spark, kafkaBrokers, topic, avroSchema)

    // Print Kafka Stream to Console
    df.writeStream
      .format("console")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .start()

    // Write Random Strings to MongoDB
    val randomDf = df.withColumn("random_string", lit(rand().toString))
    writeToMongoDB(randomDf, mongoUri)

    spark.streams.awaitAnyTermination()
  }


  def readFromKafka(spark: SparkSession, brokers: String, topic: String, schema: Schema): DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest") // Set to consume from the earliest offset
      .load()
      .select(from_avro(col("value"), schema.toString).as("data"))
  }

  def writeToMongoDB(df: DataFrame, mongoUri: String): Unit = {
    df.writeStream
      .format("mongodb")
      .option("uri", mongoUri)
      .option("database", "test-raw-data-database")
      .option("collection", "test-raw-data-collection")
      .option("checkpointLocation", "/app")
      .outputMode("append")
      .start()
      .awaitTermination()

  }
}
