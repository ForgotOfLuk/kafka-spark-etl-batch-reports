import sbt._

object Dependencies {
  // Versions
  val confluentVersion = "7.5.1"
  val kafkaVersion = "2.8.1"

  // dependencies for the common project
  val commonDependencies: Seq[ModuleID] = Seq(
    "org.apache.kafka" %% "kafka" % kafkaVersion,
    "io.confluent" % "kafka-avro-serializer" % confluentVersion,
    "org.apache.avro" % "avro" % "1.11.0"
  )

  // Specific dependencies for the mockData project
  val mockDataDependencies: Seq[ModuleID] = Seq(
  )

  // Specific dependencies for the dataProcessing project
  val dataProcessingDependencies: Seq[ModuleID] = Seq(
  )
}
