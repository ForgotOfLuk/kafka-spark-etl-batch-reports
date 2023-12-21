package utils

import com.miniclip.avro.{InAppPurchaseEvent, InitEvent, MatchEvent}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import common.model.{EventGenerator, ReferenceData}
import model.KafkaProducers
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration.{DAYS, SECONDS}
import scala.util.{Random, Try}

object Utils extends LazyLogging {

  // Generates and sends startup data to Kafka topics
  private val userIds = EventGenerator.generateUserIds(100)

  def loadConfig(): Try[(Config, Config)] = Try {
    val config = ConfigFactory.load()
    val kafkaConfig = config.getConfig("mock-data.kafka")
    val mockConfig = config.getConfig("mock-data.mock")
    (kafkaConfig, mockConfig)
  }

  def setupKafkaProducers(kafkaConfig: Config): KafkaProducers = {
    val brokers = sys.env.getOrElse("KAFKA_BROKERS", kafkaConfig.getString("brokers"))
    val schemaRegistryUrl = sys.env.getOrElse("SCHEMA_REGISTRY_URL", kafkaConfig.getString("schemaRegistryUrl"))

    logger.info("Setting up Kafka producers with brokers: " + brokers + " and schema registry URL: " + schemaRegistryUrl)
    KafkaProducers(
      initEventProducer = KafkaProducerUtils.createAvroProducer[InitEvent](brokers, schemaRegistryUrl),
      matchEventProducer = KafkaProducerUtils.createAvroProducer[MatchEvent](brokers, schemaRegistryUrl),
      inAppPurchaseEventProducer = KafkaProducerUtils.createAvroProducer[InAppPurchaseEvent](brokers, schemaRegistryUrl),
      globalKtableProducers = KafkaProducerUtils.createStringProducer[String](brokers),
    )
  }

  def generateData(kafkaProducers: KafkaProducers, kafkaConfig: Config, mockConfig: Config): Unit = {
    val topics = kafkaConfig.getConfig("topics")
    val globalKTableTopics = kafkaConfig.getConfig("global-ktable-topics")
    populateGlobalKTableTopics(kafkaProducers.globalKtableProducers, globalKTableTopics)

    val startupDataDays = mockConfig.getInt("startupDataDays")
    val eventIntervalSeconds = mockConfig.getInt("eventIntervalSeconds")
    val liveDataIntervalSeconds = mockConfig.getInt("liveDataIntervalSeconds")
    val errorProbability = mockConfig.getDouble("errorProbability")

    logger.info("Generating startup data")
    generateStartupData(kafkaProducers, topics, startupDataDays, eventIntervalSeconds, errorProbability)

    logger.info("Scheduling live data generation")
    val executor = Executors.newSingleThreadScheduledExecutor()
    executor.scheduleAtFixedRate(() => generateLiveData(kafkaProducers, topics, liveDataIntervalSeconds, errorProbability), 0, liveDataIntervalSeconds, TimeUnit.SECONDS)

    addShutdownHook(kafkaProducers, executor)
  }

  private def populateGlobalKTableTopics(producer: KafkaProducer[String, String], globalKTableTopics: Config): Unit = {
    val platformsTopic = globalKTableTopics.getString("platforms")
    ReferenceData.platforms.foreach { case (key, value) =>
      producer.send(new ProducerRecord[String, String](platformsTopic, key, value))
    }

    val countriesTopic = globalKTableTopics.getString("countries")
    ReferenceData.countries.foreach { case (key, value) =>
      producer.send(new ProducerRecord[String, String](countriesTopic, key, value))
    }

    val devicesTopic = globalKTableTopics.getString("devices")
    ReferenceData.devices.foreach { case (key, value) =>
      producer.send(new ProducerRecord[String, String](devicesTopic, key, value))
    }

    val productsTopic = globalKTableTopics.getString("products")
    ReferenceData.products.foreach { case (key, value) =>
      producer.send(new ProducerRecord[String, String](productsTopic, key, value))
    }
  }

  private def addShutdownHook(kafkaProducers: KafkaProducers, executor: java.util.concurrent.ExecutorService): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      kafkaProducers.closeProducers()
      executor.shutdown()
      logger.info("Mock data service shutting down")
    }))
  }

  private def generateStartupData(kafkaProducers: KafkaProducers, topics: Config, startupDataDays: Int, eventIntervalSeconds: Int, errorProbability: Double): Unit = {
    val startTime = System.currentTimeMillis() - DAYS.toMillis(startupDataDays)
    val endTime = System.currentTimeMillis()
    val intervalMillis = SECONDS.toMillis(eventIntervalSeconds)

    userIds.foreach { userId =>
      // Iterate over the time range in specified intervals
      LazyList.iterate(startTime)(_ + intervalMillis).takeWhile(_ <= endTime).foreach { eventTime =>
        generateAndSendEventsForUser(userId, kafkaProducers, topics, eventTime, eventIntervalSeconds, errorProbability, isLiveData = false)
      }
    }

    logger.info("Startup data generation completed")
  }
  // Generates and sends live data to Kafka topics
  private def generateLiveData(kafkaProducers: KafkaProducers, topics: Config, eventIntervalSeconds: Int, errorProbability: Double): Unit = {
    logger.info("Generating live data...")

    userIds.foreach { userId =>
      generateAndSendEventsForUser(userId, kafkaProducers, topics, System.currentTimeMillis(), eventIntervalSeconds, errorProbability, isLiveData = true)
    }

    logger.info("Live data generation completed")
  }

  // Helper method to generate and send events for a single user
  private def generateAndSendEventsForUser(userId: String, kafkaProducers: KafkaProducers, topics: Config, startTime: Long, eventIntervalSeconds: Int, errorProbability: Double, isLiveData: Boolean): Unit = {
    Try {
      sendInitEventIfNeeded(userId, kafkaProducers, topics, startTime, errorProbability, isLiveData)
      sendInAppPurchaseEvents(userId, kafkaProducers, topics, startTime, eventIntervalSeconds, errorProbability, isLiveData)
      sendMatchEvents(userId, kafkaProducers, topics, startTime, eventIntervalSeconds, errorProbability, isLiveData)
    } recover {
      case e: Exception => logger.error(s"Error generating events for user $userId: ${e.getMessage}", e)
    }
  }

  // Sends an InitEvent if applicable based on error probability and whether it's live data
  private def sendInitEventIfNeeded(userId: String, kafkaProducers: KafkaProducers, topics: Config, startTime: Long, errorProbability: Double, isLiveData: Boolean): Unit = {
    if (!isLiveData || Random.nextInt(90) == 0) {
      val initEventTime = if (isLiveData) startTime else startTime + Random.nextLong(SECONDS.toMillis(1))
      val initEvent = EventGenerator.generateInitEvent(initEventTime, userId, errorProbability)
      KafkaProducerUtils.sendRecord(kafkaProducers.initEventProducer, topics.getString("init"), s"$userId", initEvent)
      logger.debug(s"InitEvent sent for user $userId at $initEventTime")
    }
  }

  // Generates and sends InAppPurchaseEvents
  private def sendInAppPurchaseEvents(userId: String, kafkaProducers: KafkaProducers, topics: Config, startTime: Long, eventIntervalSeconds: Int, errorProbability: Double, isLiveData: Boolean): Unit = {
    val numberOfEvents = if (isLiveData) Random.nextInt(6) else 3 + Random.nextInt(48)
    (1 to numberOfEvents).foreach { i =>
      val eventTime = startTime + i * SECONDS.toMillis(eventIntervalSeconds)
      val inAppPurchaseEvent = EventGenerator.generateInAppPurchaseEvent(eventTime, userId, errorProbability)
      KafkaProducerUtils.sendRecord(kafkaProducers.inAppPurchaseEventProducer, topics.getString("in_app_purchase"), s"$userId", inAppPurchaseEvent)
      logger.debug(s"InAppPurchaseEvent sent for user $userId at $eventTime")
    }
  }

  // Generates and sends MatchEvents
  private def sendMatchEvents(userId: String, kafkaProducers: KafkaProducers, topics: Config, startTime: Long, eventIntervalSeconds: Int, errorProbability: Double, isLiveData: Boolean): Unit = {
    val numberOfEvents = if (isLiveData) Random.nextInt(6) else 3 + Random.nextInt(48)
    (1 to numberOfEvents).foreach { i =>
      val eventTime = startTime + i * SECONDS.toMillis(eventIntervalSeconds)
      val matchEvent = EventGenerator.generateMatchEvent(eventTime, userId, Random.shuffle(userIds).head, errorProbability)
      KafkaProducerUtils.sendRecord(kafkaProducers.matchEventProducer, topics.getString("match"), s"$userId", matchEvent)
      logger.debug(s"MatchEvent sent for user $userId at $eventTime")
    }
  }
}
