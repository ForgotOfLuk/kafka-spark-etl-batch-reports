package schema.registry.utils

import scalaj.http.{Http, HttpResponse}
import scala.io.Source
import scala.util.{Try, Using}

object SchemaRegistryClient {

  def registerSchema(schemaRegistryUrl: String, topic: String, schemaFileName: String): Unit = {
    val schemaFilePath = s"common/src/main/scala/common/model/schemas/avro/$schemaFileName.avsc"

    val result = Try {
      Using.resource(Source.fromFile(schemaFilePath)) { source =>
        val schemaString = source.mkString
        val response: HttpResponse[String] = Http(s"$schemaRegistryUrl/subjects/$topic-value/versions")
          .postData(s"""{"schema": $schemaString}""")
          .header("Content-Type", "application/vnd.schemaregistry.v1+json")
          .asString

        if (response.is2xx) {
          println(s"Schema for $topic registered successfully.")
        } else {
          throw new Exception(s"Failed to register schema for $topic: ${response.statusLine}")
        }
      }
    }

    result match {
      case scala.util.Success(_) => println("Operation successful")
      case scala.util.Failure(ex) => println(s"An error occurred: ${ex.getMessage}")
    }
  }
}
