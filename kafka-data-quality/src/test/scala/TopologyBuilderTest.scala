import com.miniclip.avro.InitEvent
import com.typesafe.config.{Config, ConfigFactory}
import common.model.{EventGenerator, ReferenceData}
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.streams._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.TopologyBuilder

import java.util.{Collections, Properties}

class TopologyBuilderTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  val MOCK_SCHEMA_REGISTRY_URL = "mock://127.0.0.1:8081"
  // Load configuration
  val config: Config = ConfigFactory.load()
  val kafkaConfig: Config = config.getConfig("data-quality.kafka")
  val inputTopics: Config = kafkaConfig.getConfig("input-topics")
  val outputTopics: Config = kafkaConfig.getConfig("output-topics")
  val globalKTableTopics: Config = kafkaConfig.getConfig("global-ktable-topics")

  //Create I/O topics:
  var initEventInputTopic: TestInputTopic[String, InitEvent] = _
  var initEventOutputTopic: TestOutputTopic[String, InitEvent] = _
  var initEventSideOutputTopic: TestOutputTopic[String, InitEvent] = _
  //Create globalKTable topics
  var platformsInputTopic: TestInputTopic[String, String] = _
  var countriesInputTopic: TestInputTopic[String, String] = _

  var testDriver: TopologyTestDriver = _
  before {
    val initEventSerde = makeSerializer


    val builder = new StreamsBuilder()
    val topologyBuilder = new TopologyBuilder(builder, "testConfig", kafkaConfig.getString("schemaRegistryUrl"))
    val topology: Topology = topologyBuilder.buildTopology()

    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("brokers"))
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL)

    // Set the default key serde to String
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass.getName)
    // Set the default value serde to SpecificAvroSerde
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[SpecificAvroSerde[_]].getName)

    testDriver = new TopologyTestDriver(topology, props)

    // Create input topics for test data
    initEventInputTopic = testDriver.createInputTopic(inputTopics.getString("init"), stringSerde.serializer(), initEventSerde.serializer())
    platformsInputTopic = testDriver.createInputTopic(globalKTableTopics.getString("platforms"), stringSerde.serializer(), stringSerde.serializer())
    countriesInputTopic = testDriver.createInputTopic(globalKTableTopics.getString("countries"), stringSerde.serializer(), stringSerde.serializer())

    initEventOutputTopic = testDriver.createOutputTopic(outputTopics.getString("init"), stringSerde.deserializer(), initEventSerde.deserializer())
    initEventSideOutputTopic = testDriver.createOutputTopic(outputTopics.getString("init_side_output"), stringSerde.deserializer(), initEventSerde.deserializer())


    // Populate GlobalKTable backing topics with reference data
    ReferenceData.platforms.foreach { case (key, value) =>
      platformsInputTopic.pipeInput(key, value)
    }

    ReferenceData.countries.foreach { case (key, value) =>
      countriesInputTopic.pipeInput(key, value)
    }
  }

  private def makeSerializer = {
    val mockSchemaRegistryClient = new MockSchemaRegistryClient()

    // Register schemas for all topics that will use Avro serialization
    val schema = InitEvent.SCHEMA$  // Use the correct schema for your Avro types

    mockSchemaRegistryClient.register(inputTopics.getString("init") + "-value", schema)
    mockSchemaRegistryClient.register(outputTopics.getString("init") + "-value", schema)
    mockSchemaRegistryClient.register(outputTopics.getString("init_side_output") + "-value", schema)

    val serdeConfig = Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL)
    val initEventSerde = new SpecificAvroSerde[InitEvent](mockSchemaRegistryClient)
    initEventSerde.configure(serdeConfig, false) // false for value serde
    initEventSerde
  }

  after {
    testDriver.close()
  }

  "Topology" should "correctly process InitEvents" in {

//    // Create a correct InitEvent
//    val testEvent = EventGenerator.generateInitEvent(System.currentTimeMillis(), "miniclip", 0)
//
//    // Send the test event to the input topic
//    initEventInputTopic.pipeInput("testKey", testEvent)
//
//    // Read the result from the output topic
//    val result: InitEvent = initEventOutputTopic.readValue()
//
//    // Assertions
//    result should not be null
//    result.userId shouldBe "miniclip"
  }
}
