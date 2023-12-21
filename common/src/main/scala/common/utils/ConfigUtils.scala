package common.utils

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigUtils {
  private val config: Config = ConfigFactory.load()
  def getKafkaConfig(projectName: String): Config = config.getConfig(s"$projectName.kafka")

  def getBootstrapServers(projectName: String): String = sys.env.getOrElse("KAFKA_BROKERS", getKafkaConfig(projectName).getString("brokers"))
  def getSchemaRegistryUrl(projectName: String): String = sys.env.getOrElse("SCHEMA_REGISTRY_URL", getKafkaConfig(projectName).getString("schemaRegistryUrl"))

  def getInputTopic(projectName: String, name: String): String = getKafkaConfig(projectName).getString(s"input-topics.$name")
  def getGlobalKTableTopic(projectName: String, name: String): String = getKafkaConfig(projectName).getString(s"global-ktable-topics.$name")
  def getOutputTopic(projectName: String, name: String): String = getKafkaConfig(projectName).getString(s"output-topics.$name")
}
