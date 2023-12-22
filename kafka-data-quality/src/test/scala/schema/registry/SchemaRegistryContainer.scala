package schema.registry

import org.testcontainers.containers.{GenericContainer, Network}
import org.testcontainers.containers.wait.strategy.Wait

/**
 * A Schema Registry container for use with Kafka in a Testcontainers environment.
 *
 * @param version The version of the Schema Registry to use.
 */
class SchemaRegistryContainer(version: String) extends GenericContainer[SchemaRegistryContainer](s"confluentinc/cp-schema-registry:$version") {
  val SCHEMA_REGISTRY_PORT: Int = 28081

  // Expose the necessary port and set a health check.
  withExposedPorts(SCHEMA_REGISTRY_PORT)
  waitingFor(Wait.forHttp("/subjects").forStatusCode(200)
    .withStartupTimeout(java.time.Duration.ofMinutes(2))) // Adjust the startup timeout as necessary

  /**
   * Configure the container to use the same network as the Kafka container.
   * This ensures that the Schema Registry can communicate with Kafka.
   *
   * @param network The network on which Kafka is running.
   * @param bootstrapServers The bootstrap servers of Kafka.
   * @return This SchemaRegistryContainer instance.
   */
  def withKafka(network: Network, bootstrapServers: String): SchemaRegistryContainer = {
    withNetwork(network)
    withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
    withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:28081")
    withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + bootstrapServers)
    this
  }

  /**
   * Get the URL of the running Schema Registry container.
   *
   * @return The URL of the Schema Registry.
   */
  def getUrl: String = s"http://${getHost}:${getMappedPort(SCHEMA_REGISTRY_PORT)}"
}

object SchemaRegistryContainer {
  /**
   * Factory method to create a SchemaRegistryContainer with a specified version.
   * Defaults to the latest version if not specified.
   *
   * @param version The version of the Schema Registry, defaulting to "latest".
   * @return A new instance of SchemaRegistryContainer.
   */
  def apply(version: String = "latest"): SchemaRegistryContainer = new SchemaRegistryContainer(version)
}
