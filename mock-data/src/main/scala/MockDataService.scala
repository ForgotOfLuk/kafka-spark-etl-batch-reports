import com.miniclip.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import com.typesafe.config.{Config, ConfigFactory}
import common.KafkaAvroProducer
import model.KafkaProducers

import java.util.concurrent.{Executors, TimeUnit}

object MockDataService {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val kafkaConfig = config.getConfig("mock-data.kafka")
    val mockConfig = config.getConfig("mock-data.mock")

    // Extract configurations
    val brokers = kafkaConfig.getString("brokers")
    val schemaRegistryUrl = kafkaConfig.getString("schemaRegistryUrl")
    val topics = kafkaConfig.getConfig("topics")
    val startupDataDays = mockConfig.getInt("startupDataDays")
    val eventIntervalSeconds = mockConfig.getInt("eventIntervalSeconds")
    val liveDataIntervalSeconds = mockConfig.getInt("liveDataIntervalSeconds")
    val errorProbability = mockConfig.getDouble("errorProbability")

    // Kafka producer setup
    val kafkaProducers = KafkaProducers(
      initEventProducer = KafkaAvroProducer.createProducer[InitEvent](brokers, schemaRegistryUrl),
      matchEventProducer = KafkaAvroProducer.createProducer[MatchEvent](brokers, schemaRegistryUrl),
      inAppPurchaseEventProducer = KafkaAvroProducer.createProducer[InAppPurchaseEvent](brokers, schemaRegistryUrl)
    )

    // Generate startup data
    generateStartupData(kafkaProducers, topics, startupDataDays, eventIntervalSeconds, errorProbability)

    // Start generating live data
    val executor = Executors.newSingleThreadScheduledExecutor()
    executor.scheduleAtFixedRate(() => {
      generateLiveData(kafkaProducers, topics, errorProbability)
    }, 0, liveDataIntervalSeconds, TimeUnit.SECONDS)

    // Add shutdown hook
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      kafkaProducers.closeProducers()
      executor.shutdown()
    }))
  }

  def generateStartupData(kafkaProducers: KafkaProducers, topics: Config, startupDataDays: Int, eventIntervalSeconds: Int, errorProbability: Double): Unit = {
  }


  def generateLiveData(kafkaProducers: KafkaProducers, topics: Config, errorProb: Double): Unit = {
  }
}
