
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

import Dependencies.*
import sbtavrohugger.SbtAvrohugger.autoImport.*
import MergeStrategyBuilder.*

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

// Common project settings
lazy val sparkCommon = (project in file("spark-common"))
  .dependsOn(common)
  .settings(
    resolvers ++= commonResolvers,
    name := "Miniclip-SparkCommon",
    // Common dependencies for all subprojects
    libraryDependencies ++= sparkProvidedDependencies,
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
    assembly / assemblyMergeStrategy := mergeStrategy
  )

// Kafka data quality project settings
lazy val dataQuality = (project in file("kafka-data-quality"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataQuality",
    // Dependencies specific to the data-quality project
    libraryDependencies ++= dataStreamsDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("KafkaDataQualityServiceApp"),
    assembly / assemblyJarName := "kafka-data-quality-assembly.jar",
    // Handling of merge conflicts during assembly
    assembly / assemblyMergeStrategy := mergeStrategy
  )

// Kafka data quality project settings
lazy val dataEnrichment = (project in file("kafka-data-enrichment"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataEnrichment",
    // Dependencies specific to the data-quality project
    libraryDependencies ++= dataStreamsDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("KafkaDataEnrichmentServiceApp"),
    assembly / assemblyJarName := "kafka-data-enrichment-assembly.jar",
    // Handling of merge conflicts during assembly
    assembly / assemblyMergeStrategy := mergeStrategy
  )

// Spark daily aggregator project settings
lazy val sparkDailyAggregation = (project in file("spark-daily-aggregation"))
  .dependsOn(sparkCommon)
  .settings(
    name := "Miniclip-SparkDailyAggregation",
    libraryDependencies ++= sparkProvidedDependencies ++ sparkDailyDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("SparkDailyAggregatorService"),
    assembly / assemblyJarName := "spark-daily-aggregation-assembly.jar",
    assembly / assemblyOption ~= {
      _.withIncludeScala(false)
    },
    // Handling of merge conflicts during assembly
    assembly / assemblyMergeStrategy := sparkDailyMergeStrategy,
    Compile / fullClasspath ++= (sparkCommon / Compile / fullClasspath).value.files
  )

// Spark daily aggregator project settings
lazy val sparkMinutePurchaseAggregation = (project in file("spark-minute-purchase-aggregation"))
  .dependsOn(sparkCommon)
  .settings(
    name := "Miniclip-SparkMinutePurchaseAggregation",
    libraryDependencies ++= sparkProvidedDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("SparkMinutePurchaseAggregationService"),
    assembly / assemblyJarName := "spark-minute-purchase-aggregation-assembly.jar",
    assembly / assemblyOption ~= {
      _.withIncludeScala(false)
    },
    // Handling of merge conflicts during assembly
    MergeStrategyBuilder.sparkMergeStrategy,
    Compile / fullClasspath ++= (sparkCommon / Compile / fullClasspath).value.files
  )
// Spark daily aggregator project settings
lazy val sparkMinuteMatchAggregation = (project in file("spark-minute-match-aggregation"))
  .dependsOn(sparkCommon)
  .settings(
    name := "Miniclip-SparkMinuteMatchAggregation",
    libraryDependencies ++= sparkProvidedDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("SparkMinuteMatchAggregatorService"),
    assembly / assemblyJarName := "spark-minute-match-aggregation-assembly.jar",
    assembly / assemblyOption ~= {
      _.withIncludeScala(false)
    },
    // Handling of merge conflicts during assembly
    MergeStrategyBuilder.sparkMergeStrategy,
    Compile / fullClasspath ++= (sparkCommon / Compile / fullClasspath).value.files
  )

// Root project settings
lazy val root = (project in file("."))
  .aggregate(common, mockData, dataQuality, dataEnrichment, sparkDailyAggregation, sparkMinutePurchaseAggregation, sparkMinuteMatchAggregation)
  .settings(
    name := "Miniclip"
  )
