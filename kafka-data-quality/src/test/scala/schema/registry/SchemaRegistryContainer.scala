package schema.registry

import org.testcontainers.containers.{GenericContainer, Network}
import org.testcontainers.containers.wait.strategy.Wait

class SchemaRegistryContainer(version: String, network: Network) extends GenericContainer[SchemaRegistryContainer](s"confluentinc/cp-schema-registry:$version") {
  val SCHEMA_REGISTRY_PORT: Int = 8081

  withExposedPorts(SCHEMA_REGISTRY_PORT)
  waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
  withNetwork(network)


  def withKafka(bootstrapServers: String): SchemaRegistryContainer = {
    withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
    withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
    withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", bootstrapServers)
    this
  }

  def getUrl: String = s"http://${getHost}:${getMappedPort(SCHEMA_REGISTRY_PORT)}"
}

object SchemaRegistryContainer {
  def apply(version: String = "latest", network: Network): SchemaRegistryContainer = new SchemaRegistryContainer(version, network)
}
