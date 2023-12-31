import common.utils.ConfigUtils

object KafkaDataEnrichmentServiceApp extends App with ConfigUtils {
  private val configName = "data-enrichment"
  private val bootstrapServers = getBootstrapServers(configName)
  private val schemaRegistryUrl = getSchemaRegistryUrl(configName)

  private val service = new KafkaDataEnrichmentService(bootstrapServers, schemaRegistryUrl)
  service.start()
}
