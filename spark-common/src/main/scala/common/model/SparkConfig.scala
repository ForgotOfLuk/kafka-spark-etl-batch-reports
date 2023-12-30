package common.model

case class SparkConfig(appName: String, masterUrl: String, kafkaConfig: KafkaConfig, mongoConfig: MongoConfig)
case class MongoConfig(mongoUri: String, mongoDb: String, mongoCollection: String, mongoEnrichedCollection: String)
case class KafkaConfig(kafkaBrokers: String, offsetConfigKey: String, offsetConfigValue: String, initEventTopic: String, matchEventTopic: String, purchaseEventTopic: String)

object SparkConfig {
  def fromEnv(): SparkConfig = {
    val appName = sys.env.getOrElse("APP_NAME", "DefaultAppName")
    val masterUrl = sys.env.getOrElse("MASTER_URL", "local[*]")
    val kafkaBrokers = sys.env.getOrElse("KAFKA_BROKERS", "miniclip_kafka:9092")
    val initEventTopic = sys.env.getOrElse("KAFKA_INIT_TOPIC", "init_validated")
    val matchEventTopic = sys.env.getOrElse("KAFKA_MATCH_TOPIC", "match_validated")
    val purchaseEventTopic = sys.env.getOrElse("KAFKA_PURCHASE_TOPIC", "in_app_purchase_validated")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://mongodb:27017")
    val mongoDb = sys.env.getOrElse("MONGO_DB", "mongodb://mongodb:27017")
    val mongoCollection = sys.env.getOrElse("MONGO_COLLECTION", "error")
    val mongoEnrichedCollection = sys.env.getOrElse("MONGO_ENRICHED_COLLECTION", "error")
    val offsetConfigKey = sys.env.getOrElse("OFFSET_CONFIG_KEY", "startingOffsets")
    val offsetConfigValue = sys.env.getOrElse("OFFSET_CONFIG_VALUE", "earliest")

    val kafkaConfig = KafkaConfig(kafkaBrokers, offsetConfigKey, offsetConfigValue, initEventTopic, matchEventTopic, purchaseEventTopic)
    val mongoConfig = MongoConfig(mongoUri, mongoDb, mongoCollection, mongoEnrichedCollection)
    SparkConfig(appName, masterUrl, kafkaConfig, mongoConfig)
  }
}
