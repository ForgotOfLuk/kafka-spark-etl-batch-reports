ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

import Dependencies.*
import sbtavrohugger.SbtAvrohugger.autoImport.*

lazy val common = (project in file("common"))
  .settings(
    resolvers ++= Seq(
      Classpaths.typesafeReleases,
      "confluent" at "https://packages.confluent.io/maven/",
    ),
    name := "Miniclip-Common",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "org.apache.kafka" %% "kafka" % kafkaVersion,
      "io.confluent" % "kafka-avro-serializer" % "7.5.1",
      "org.apache.avro" % "avro" % "1.11.0"
    ),
    //add avro generation to the compile step
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
    Compile / avroSpecificSourceDirectories += baseDirectory.value / "src/main/scala/common/model/schemas/avro",
    Compile / avroSpecificScalaSource := baseDirectory.value / "src/main/scala/common/model/schemas/avro"
)

lazy val mockData = (project in file("mock-data"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-MockData",
    libraryDependencies ++= mockDataDependencies
  )

lazy val dataProcessing = (project in file("kafka-data-processing"))
  .dependsOn(common)
  .settings(
    name := "Miniclip-KafkaDataProcessing",
    libraryDependencies ++= dataProcessingDependencies
  )

lazy val root = (project in file("."))
  .aggregate(common, mockData, dataProcessing)
  .settings(
    name := "Miniclip"
  )
