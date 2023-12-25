import sbt.*

object Dependencies {
  // Versions
  val confluentVersion = "7.5.1"
  val kafkaVersion = "3.4.0"
  val avroVersion = "1.11.0"
  val typesafeVersion = "1.4.2"
  val scalatestVersion = "3.2.15"
  val scalaLogVersion = "3.9.5"
  val logbackVersion = "1.4.7"
  val catsVersion = "2.9.0"
  val testcontainers = "1.17.6"
  val scalaHttpVersion = "2.4.2"
  val sparkVersion = "3.2.4"
  val mongoSparkVersion = "10.2.1"

  // dependencies for the common project
  val commonDependencies: Seq[ModuleID] = Seq(
    "io.confluent" % "kafka-avro-serializer" % confluentVersion,
    "org.apache.avro" % "avro" % avroVersion,
    "com.typesafe" % "config" % typesafeVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLogVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    //Test dependencies
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  )

  // Specific dependencies for the mockData project
  val mockDataDependencies: Seq[ModuleID] = Seq(
    "org.apache.kafka" %% "kafka" % kafkaVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  )

  // Specific dependencies for the dataQuality project
  val dataQualityDependencies: Seq[ModuleID] = Seq(
    "io.confluent" % "kafka-streams-avro-serde" % confluentVersion,
    "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
    "org.apache.kafka" % "kafka-clients" % kafkaVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    //Test dependencies
    "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion % Test,
    "org.testcontainers" % "testcontainers" % testcontainers,
    "org.testcontainers" % "kafka" % testcontainers,
    "org.scalaj" %% "scalaj-http" % scalaHttpVersion,
  )

  // Specific dependencies for the sparkDailyBatchAggregation project
  val sparkBatchAggregatorDependencies: Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
    "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
    "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
    "org.apache.spark" %% "spark-avro" % sparkVersion % Provided,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % Provided,
    "org.mongodb.spark" %% "mongo-spark-connector" % mongoSparkVersion,

    // Test dependencies
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
  )

  val commonResolvers: Seq[Resolver] = Seq(
    Classpaths.typesafeReleases,
    "confluent" at "https://packages.confluent.io/maven/",
  )
}
