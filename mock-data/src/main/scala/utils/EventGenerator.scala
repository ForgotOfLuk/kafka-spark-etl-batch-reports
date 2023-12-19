package utils

import com.miniclip.avro._
import com.typesafe.scalalogging.LazyLogging
import scala.util.Random

// EventGenerator object to generate different types of Kafka events
object EventGenerator extends LazyLogging {
  private val random = new Random()
  private val platforms = List("Android", "PS5", "GameCube", "Xbox", "Switch")
  private val devices = (1 to 100).map(_ => java.util.UUID.randomUUID().toString).toList
  private val productIds = (1 to 10).map(_ => java.util.UUID.randomUUID().toString).toList
  private val countries = List("USA", "Canada", "Brazil", "UK", "Germany", "France", "Japan", "China", "Australia", "Russia", "India", "South Africa", "Spain", "Italy", "Mexico", "Sweden", "Norway", "Finland", "Denmark", "Netherlands")

  // Generates a list of user IDs with a mix of UUIDs and numeric strings
  def generateUserIds(count: Int): List[String] = {
    (1 to count).map { _ =>
      if (Random.nextInt(3) == 0) {
        Random.nextLong().abs.toString
      } else {
        java.util.UUID.randomUUID().toString
      }
    }.toList
  }

  // Generates an InitEvent with a possibility of introducing errors based on the errorProbability
  def generateInitEvent(time: Long, userId: String, errorProbability: Double): InitEvent = {
    logger.debug(s"Generating InitEvent with errorProbability: $errorProbability")
    val platform = chooseRandomWithError(platforms, errorProbability, randomWrongString())
    val country = chooseRandomWithError(countries, errorProbability, randomWrongString(10))
    val modifiedUserId = if (random.nextDouble() < errorProbability) randomWrongString() else userId
    InitEvent(EventTypeInit.init, time, modifiedUserId, country, platform)
  }

  // Generates an InAppPurchaseEvent with a possibility of introducing errors
  def generateInAppPurchaseEvent(time: Long, userId: String, errorProbability: Double): InAppPurchaseEvent = {
    logger.debug(s"Generating InAppPurchaseEvent with errorProbability: $errorProbability")
    val purchaseValue = if (random.nextDouble() < errorProbability) random.nextDouble() * 10000 else 1 + random.nextDouble() * 99
    val productId = chooseRandomWithError(productIds, errorProbability, randomWrongString(random.nextInt(12)))
    val modifiedUserId = if (random.nextDouble() < errorProbability) randomWrongString() else userId
    InAppPurchaseEvent(EventTypeInAppPurchase.in_app_purchase, time, purchaseValue, modifiedUserId, productId)
  }

  // Generates a MatchEvent with randomized attributes and error probability
  def generateMatchEvent(time: Long, userA: String, userB: String, errorProbability: Double): MatchEvent = {
    logger.debug(s"Generating MatchEvent with errorProbability: $errorProbability")
    val winner = if (random.nextBoolean()) userA else userB
    val gameTier = if (random.nextDouble() < errorProbability) random.nextInt(1000) else 1 + random.nextInt(9)
    val duration = if (random.nextDouble() < errorProbability) random.nextInt(10000) else 30 + random.nextInt(141)
    val userAPostMatchInfo = randomUserPostmatch(errorProbability)
    val userBPostMatchInfo = randomUserPostmatch(errorProbability)
    MatchEvent(EventTypeMatch.`match`, time, userA, userB, userAPostMatchInfo, Some(userBPostMatchInfo), winner, gameTier, duration)
  }

  // Helper methods
  private def randomUserPostmatch(errorProbability: Double): UserPostmatchInfo =
    UserPostmatchInfo(randomPositiveLongWithError(errorProbability), randomPositiveLongWithError(errorProbability), chooseRandomWithError(devices, errorProbability, randomWrongString()), chooseRandomWithError(platforms, errorProbability, randomWrongString()))

  private def randomPositiveLongWithError(errorProbability: Double, max: Long = 1000): Long =
    if (random.nextDouble() < errorProbability) random.nextLong() * -1 * 200 else 1 + random.nextLong(max)

  private def randomWrongString(length: Int = 5): String =
    Random.alphanumeric.take(length).mkString

  private def chooseRandomWithError[T](list: List[T], errorProbability: Double, errorValue: => T): T = {
    if (random.nextDouble() < errorProbability) errorValue else list(random.nextInt(list.length))
  }
}
