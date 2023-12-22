package utils

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

object Utils {
  def loadConfig(): Try[(Config, Config)] = Try {
    val config = ConfigFactory.load()
    val kafkaConfig = config.getConfig("mock-data.kafka")
    val mockConfig = config.getConfig("mock-data.mock")
    (kafkaConfig, mockConfig)
  }
}
