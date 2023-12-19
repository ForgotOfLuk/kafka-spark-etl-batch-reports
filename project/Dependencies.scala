import sbt.*

object Dependencies {
  // Versions
  val confluentVersion = "7.5.1"
  val kafkaVersion = "2.8.1"
  val avroVersion = "1.11.0"
  val typesafeVersion = "1.4.2"
  val scalatestVersion = "3.2.15"
  val scalaLogVersion = "3.9.5"
  val logbackVersion = "1.4.7"

  // dependencies for the common project
  val commonDependencies: Seq[ModuleID] = Seq(
    "org.apache.avro" % "avro" % avroVersion,
    "com.typesafe" % "config" % typesafeVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLogVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  )
  // Specific dependencies for the mockData project
  val mockDataDependencies: Seq[ModuleID] = Seq(
    "io.confluent" % "kafka-avro-serializer" % confluentVersion,
    "org.apache.kafka" %% "kafka" % kafkaVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  )

  // Specific dependencies for the dataProcessing project
  val dataProcessingDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  )

  val commonResolvers: Seq[Resolver] = Seq(
    Classpaths.typesafeReleases,
    "confluent" at "https://packages.confluent.io/maven/",
  )
}
