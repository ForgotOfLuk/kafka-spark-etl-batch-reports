import common.utils.ConfigUtils

object KafkaDataQualityServiceApp extends App with ConfigUtils {
  private val configName = "data-quality"
  private val bootstrapServers = getBootstrapServers(configName)
  private val schemaRegistryUrl = getSchemaRegistryUrl(configName)

  val service = new KafkaDataQualityService(bootstrapServers, schemaRegistryUrl)
  service.start()
}
