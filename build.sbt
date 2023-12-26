ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

import Dependencies.*
import sbtavrohugger.SbtAvrohugger.autoImport.*

// Common project settings
lazy val common = (project in file("common"))
  .settings(
    resolvers ++= commonResolvers,
    name := "Miniclip-Common",
    // Common dependencies for all subprojects
    libraryDependencies ++= commonDependencies,
    // Avro-specific settings
    // Add avro generation to the compile step
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
    // Define Avro schema source directory
    Compile / avroSpecificSourceDirectories += baseDirectory.value / "src/main/scala/common/model/schemas/avro",
    // Define output directory for generated Avro Scala sources
    Compile / avroSpecificScalaSource := baseDirectory.value / "src/main/scala/common/model/schemas/avro"
  )

// Mock data project settings
lazy val mockData = (project in file("mock-data"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-MockData",
    // Dependencies specific to the mock-data project
    libraryDependencies ++= mockDataDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("MockDataService"),
    assembly / assemblyJarName := "mock-data-assembly.jar",
    // Handling of merge conflicts during assembly
    MergeStrategyBuilder.mergeStrategy
  )

// Kafka data quality project settings
lazy val dataQuality = (project in file("kafka-data-quality"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataQuality",
    // Dependencies specific to the data-quality project
    libraryDependencies ++= dataQualityDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("KafkaDataQualityServiceApp"),
    assembly / assemblyJarName := "kafka-data-quality-assembly.jar",
    // Handling of merge conflicts during assembly
    MergeStrategyBuilder.mergeStrategy
  )


// Spark daily aggregator project settings
lazy val sparkDailyAggregation = (project in file("spark-daily-aggregation"))
  .dependsOn(common)
  .settings(
      name := "Miniclip-SparkDailyAggregation",
      // Dependencies specific to the Spark aggregator project
      libraryDependencies ++= sparkAggregatorDependencies,
      // Assembly plugin settings for building a fat JAR
      assembly / mainClass := Some("SparkDailyAggregatorService"),
      assembly / assemblyJarName := "spark-daily-aggregation-assembly.jar",
      // or as follows
      assembly / assemblyOption ~= {
          _.withIncludeScala(false)
      },
      // Handling of merge conflicts during assembly
      MergeStrategyBuilder.sparkMergeStrategy,
      Compile / fullClasspath ++= (common / Compile / fullClasspath).value.files
  )

// Spark daily aggregator project settings
lazy val sparkMinuteAggregation = (project in file("spark-minute-aggregation"))
  .dependsOn(common)
  .settings(
      name := "Miniclip-SparkMinuteAggregation",
      // Dependencies specific to the Spark aggregator project
      libraryDependencies ++= sparkAggregatorDependencies,
      // Assembly plugin settings for building a fat JAR
      assembly / mainClass := Some("SparkMinuteAggregatorService"),
      assembly / assemblyJarName := "spark-minute-aggregation-assembly.jar",
      // or as follows
      assembly / assemblyOption ~= {
          _.withIncludeScala(false)
      },
      // Handling of merge conflicts during assembly
      MergeStrategyBuilder.sparkMergeStrategy,
      Compile / fullClasspath ++= (common / Compile / fullClasspath).value.files
  )

// Root project settings
lazy val root = (project in file("."))
  .aggregate(common, mockData, dataQuality, sparkDailyAggregation, sparkMinuteAggregation)
  .settings(
    name := "Miniclip"
  )
