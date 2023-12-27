package common.model

case class SparkConfig(appName: String, masterUrl: String, kafkaConfig: KafkaConfig, mongoConfig: MongoConfig)
case class MongoConfig(mongoUri: String, mongoCollection: String, mongoDb: String)
case class KafkaConfig(kafkaBrokers: String, initEventTopic: String)

object SparkConfig {
  def fromEnv(): SparkConfig = {
    val appName = sys.env.getOrElse("APP_NAME", "DefaultAppName")
    val masterUrl = sys.env.getOrElse("MASTER_URL", "local[*]")
    val kafkaBrokers = sys.env.getOrElse("KAFKA_BROKERS", "miniclip_kafka:9092")
    val initEventTopic = sys.env.getOrElse("KAFKA_INIT_TOPIC", "init_validated")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://mongodb:27017")
    val mongoCollection = sys.env.getOrElse("MONGO_COLLECTION", "mongodb://mongodb:27017")
    val mongoDb = sys.env.getOrElse("MONGO_DB", "mongodb://mongodb:27017")

    val kafkaConfig = KafkaConfig(kafkaBrokers, initEventTopic)
    val mongoConfig = MongoConfig(mongoUri, mongoCollection, mongoDb)

    SparkConfig(appName, masterUrl, kafkaConfig, mongoConfig)
  }
}
