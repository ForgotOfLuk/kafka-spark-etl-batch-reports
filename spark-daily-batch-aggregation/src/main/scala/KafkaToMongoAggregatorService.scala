import org.apache.avro.Schema
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.avro.functions.from_avro
import org.apache.spark.sql.functions.col

import java.io.File

object KafkaToMongoAggregatorService {
  def main(args: Array[String]): Unit = {
    val appName = sys.env.getOrElse("APP_NAME", "DefaultAppName")
    val masterUrl = sys.env.getOrElse("MASTER_URL", "local[*]") // Example: "spark://spark-master:7077"
    val kafkaBrokers = sys.env.getOrElse("KAFKA_BROKERS", "miniclip_kafka:9092")
    val topic = sys.env.getOrElse("KAFKA_TOPIC", "init-validated")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://localhost:27017/default-db")

    val spark = SparkSession.builder
      .appName(appName)
      .master(masterUrl)
      .getOrCreate()

    val avroSchema = new Schema.Parser().parse(new File("/app/schemas/init.avsc"))
    val df = readFromKafka(spark, kafkaBrokers, topic, avroSchema)

    writeToMongoDB(df, mongoUri)
    spark.stop()
  }

  def readFromKafka(spark: SparkSession, brokers: String, topic: String, schema: Schema): DataFrame = {
    spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topic)
      .load()
      .select(from_avro(col("value"), schema.toString).as("data"))
  }

  def writeToMongoDB(df: DataFrame, mongoUri: String): Unit = {
    df.write
      .format("mongo")
      .option("uri", mongoUri)
      .mode("append")
      .save()
  }
}
