import com.typesafe.config.{Config, ConfigFactory}
import common.kafka.utils.Utils.populateGlobalKTableTopics
import common.kafka.utils.{KafkaConsumerUtils, KafkaProducerUtils, Utils}
import common.kafka.{KafkaConsumers, KafkaProducers}
import common.model.EventGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.testcontainers.containers.{KafkaContainer, Network}
import org.testcontainers.utility.DockerImageName
import schema.registry.SchemaRegistryContainer

import java.time.Duration

class TopologyTest extends AnyFlatSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll {
  val network: Network = Network.newNetwork()

  // Kafka and Schema Registry containers
  val kafkaContainer: KafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
    .withNetwork(network)
  val schemaRegistryContainer: SchemaRegistryContainer = SchemaRegistryContainer(network = network)

  // Configuration loading
  val config: Config = ConfigFactory.load()
  val kafkaConfig: Config = config.getConfig("testConfig.kafka")
  val inputTopics: Config = kafkaConfig.getConfig("input-topics")
  val outputTopics: Config = kafkaConfig.getConfig("output-topics")
  val globalKTableTopics: Config = kafkaConfig.getConfig("global-ktable-topics")

  // Kafka producers
  var kafkaProducers: KafkaProducers = _

  // Kafka consumers
  var kafkaConsumers: KafkaConsumers = _

  //Service
  var service: KafkaDataQualityService = _

  override def beforeAll(): Unit = {
    kafkaContainer.start()
    Thread.sleep(30000L)

    // Connect Schema Registry to the same network as Kafka
    schemaRegistryContainer.withKafka(kafkaContainer.getBootstrapServers)
    schemaRegistryContainer.start()
    Thread.sleep(30000L)

    // Load configuration and setup Kafka producers
    kafkaProducers = Utils.setupKafkaProducers(kafkaConfig)
    kafkaConsumers = Utils.setupKafkaConsumers(kafkaConfig)

    val kafkaBootstrapServers = kafkaContainer.getBootstrapServers
    val schemaRegistryUrl = schemaRegistryContainer.getUrl

    //Send data to globalKTableTopics
    populateGlobalKTableTopics(kafkaProducers.globalKtableProducers, globalKTableTopics)

    // Create and start the service with container configurations
    service = new KafkaDataQualityService(kafkaBootstrapServers, schemaRegistryUrl)
    service.start()
  }

  override def afterAll(): Unit = {
    kafkaContainer.stop()
    schemaRegistryContainer.stop()
    kafkaProducers.closeProducers()
    kafkaConsumers.closeConsumers()
  }

  before {
    // Populate globalKTable topics
    val globalKTableTopics = kafkaConfig.getConfig("global-ktable-topics")
    Utils.populateGlobalKTableTopics(kafkaProducers.globalKtableProducers, globalKTableTopics)
  }


  "Topology" should "correctly process InitEvents" in {
    // Create a correct InitEvent
    val testEvent = EventGenerator.generateInitEvent(System.currentTimeMillis(), "miniclip", 0)

    // Send the test event to the input topic
    KafkaProducerUtils.sendRecord(kafkaProducers.initEventProducer, inputTopics.getString("init"), "miniclip", testEvent)

    // Read the result from the output topic
    val result = KafkaConsumerUtils.consumeRecords(kafkaConsumers.initEventConsumer, "init-validated", Duration.ofSeconds(30))

    // Assertions
    result should not be null
    result.get.nonEmpty shouldBe true
    result.get.size shouldBe 1
  }
}
