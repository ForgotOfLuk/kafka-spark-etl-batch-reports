import Dependencies.commonResolvers

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

import Dependencies.*
import sbtavrohugger.SbtAvrohugger.autoImport.*

lazy val common = (project in file("common"))
  .settings(
    resolvers ++= commonResolvers,
    name := "Miniclip-Common",
    libraryDependencies ++= commonDependencies,
    //add avro generation to the compile step
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
    Compile / avroSpecificSourceDirectories += baseDirectory.value / "src/main/scala/common/model/schemas/avro",
    Compile / avroSpecificScalaSource := baseDirectory.value / "src/main/scala/common/model/schemas/avro"
)

lazy val mockData = (project in file("mock-data"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-MockData",
    libraryDependencies ++= mockDataDependencies,
    assembly / mainClass := Some("MockDataService"),
    assembly / assemblyJarName := "mock-data-assembly.jar",
    ThisBuild / assemblyMergeStrategy := {
        case PathList("META-INF", xs @ _*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
        case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
        case "module-info.class" => MergeStrategy.discard
        case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard // Add this line
        case "kafka/kafka-version.properties" => MergeStrategy.first
        case x => MergeStrategy.defaultMergeStrategy(x)
    }
  )


lazy val dataQuality = (project in file("kafka-data-quality"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataQuality",
    libraryDependencies ++= dataQualityDependencies,
      assembly / mainClass := Some("KafkaDataQualityService"),
      assembly / assemblyJarName := "kafka-data-quality-assembly.jar",
      ThisBuild / assemblyMergeStrategy := {
          case PathList("META-INF", xs @ _*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
          case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
          case "module-info.class" => MergeStrategy.discard
          case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard // Add this line
          case "kafka/kafka-version.properties" => MergeStrategy.first
          case x => MergeStrategy.defaultMergeStrategy(x)
      }
  )

lazy val root = (project in file("."))
  .aggregate(common, mockData, dataQuality)
  .settings(
    name := "Miniclip"
  )
