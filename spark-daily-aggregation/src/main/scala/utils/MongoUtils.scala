package utils

import common.model.MongoConfig
import org.mongodb.scala._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Projections._

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.language.postfixOps

object MongoUtils {
  @tailrec
  def apply(mongoConfig: MongoConfig): MongoUtils = MongoUtils(mongoConfig)
}

class MongoUtils(mongoConfig: MongoConfig) {
  private val mongoClient: MongoClient = MongoClient(mongoConfig.mongoUri)
  private val database: MongoDatabase = mongoClient.getDatabase(mongoConfig.mongoDb)
  private val collection: MongoCollection[Document] = database.getCollection(mongoConfig.mongoCollection)

  def retrieveLatestTimestamp(): Option[String] = {
    val future = collection.find()
      .sort(descending("timestamp"))
      .limit(1)
      .projection(include("timestamp"))
      .toFuture()

    Try(Await.result(future, 10 seconds)) match {
      case Success(docs) if docs.nonEmpty => Some(docs.head.getString("day"))
      case Success(_) => None
      case Failure(exception) =>
        println(s"Error retrieving latest timestamp: ${exception.getMessage}")
        None
    }
  }
}