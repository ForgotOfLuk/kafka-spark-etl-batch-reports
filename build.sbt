import Dependencies.commonResolvers

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

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
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard
      case "kafka/kafka-version.properties" => MergeStrategy.first
      case x => MergeStrategy.defaultMergeStrategy(x)
    }
  )

// Kafka data quality project settings
lazy val dataQuality = (project in file("kafka-data-quality"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataQuality",
    // Dependencies specific to the data-quality project
    libraryDependencies ++= dataQualityDependencies,
    // Assembly plugin settings for building a fat JAR
    assembly / mainClass := Some("KafkaDataQualityService"),
    assembly / assemblyJarName := "kafka-data-quality-assembly.jar",
    // Handling of merge conflicts during assembly
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard
      case "kafka/kafka-version.properties" => MergeStrategy.first
      case x => MergeStrategy.defaultMergeStrategy(x)
    }
  )

// Root project settings
lazy val root = (project in file("."))
  .aggregate(common, mockData, dataQuality)
  .settings(
    name := "Miniclip"
  )
