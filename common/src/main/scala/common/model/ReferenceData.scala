package common.model

import java.util.UUID

object ReferenceData {
  // Platforms with UUIDs as keys
  val platforms: Map[String, String] = Map(
    UUID.randomUUID().toString -> "android",
    UUID.randomUUID().toString -> "ps5",
    UUID.randomUUID().toString -> "gameCube",
    UUID.randomUUID().toString -> "xbox",
    UUID.randomUUID().toString -> "switch",
    UUID.randomUUID().toString -> "pc"
  )

  // Devices with UUIDs as keys and random strings of length 7 as values
  val devices: Map[String, String] = (1 to 100).map { _ =>
    UUID.randomUUID().toString -> scala.util.Random.alphanumeric.take(7).mkString
  }.toMap

  // Product IDs with UUIDs as keys and product types as values
  val products: Map[String, String] = (1 to 10).map { i =>
    UUID.randomUUID().toString -> List("costume1", "speed boost", "cosmetics", "extra life", "power-up").apply(i % 5)
  }.toMap

  // Countries with UUIDs as keys
  val countries: Map[String, String] = Map(
    UUID.randomUUID().toString -> "USA",
    UUID.randomUUID().toString -> "Canada",
    UUID.randomUUID().toString -> "Brazil",
    UUID.randomUUID().toString -> "UK",
    UUID.randomUUID().toString -> "Germany",
    UUID.randomUUID().toString -> "France",
    UUID.randomUUID().toString -> "Japan",
    UUID.randomUUID().toString -> "China",
    UUID.randomUUID().toString -> "Australia",
    UUID.randomUUID().toString -> "Russia",
    UUID.randomUUID().toString -> "India",
    UUID.randomUUID().toString -> "South Africa",
    UUID.randomUUID().toString -> "Spain",
    UUID.randomUUID().toString -> "Italy",
    UUID.randomUUID().toString -> "Mexico",
    UUID.randomUUID().toString -> "Sweden",
    UUID.randomUUID().toString -> "Norway",
    UUID.randomUUID().toString -> "Finland",
    UUID.randomUUID().toString -> "Denmark",
    UUID.randomUUID().toString -> "Netherlands"
  )
}
