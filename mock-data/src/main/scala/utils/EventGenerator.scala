package utils

import com.miniclip.avro._
import scala.util.Random

object EventGenerator {
  private val random = new Random()
  private val platforms = List("Android", "PS5", "GameCube", "Xbox", "Switch")
  private val devices = (1 to 100).map(_ => java.util.UUID.randomUUID().toString)
  private val productIds = (1 to 10).map(_ => java.util.UUID.randomUUID().toString)
  private val countries = List("USA", "Canada", "Brazil", "UK", "Germany", "France", "Japan", "China", "Australia", "Russia", "India", "South Africa", "Spain", "Italy", "Mexico", "Sweden", "Norway", "Finland", "Denmark", "Netherlands")

  def generateInitEvent(time: Long, userId: String, errorProbability: Double): InitEvent = {
    val platform = chooseRandomWithError(platforms, errorProbability, randomWrongString())
    val country = chooseRandomWithError(countries, errorProbability, randomWrongString(10))
    val modifiedUserId = if (random.nextDouble() < errorProbability) randomWrongString() else userId
    InitEvent(EventTypeInit.init, time, modifiedUserId, country, platform)
  }

  def generateInAppPurchaseEvent(time: Long, userId: String, errorProbability: Double): InAppPurchaseEvent = {
    val purchaseValue = if (random.nextDouble() < errorProbability) random.nextDouble() * 10000 else 1 + random.nextDouble() * 99
    val productId = chooseRandomWithError(productIds, errorProbability, randomWrongString(random.nextInt(12)))
    val modifiedUserId = if (random.nextDouble() < errorProbability) randomWrongString() else userId
    InAppPurchaseEvent(EventTypeInAppPurchase.in_app_purchase, time, purchaseValue, modifiedUserId, productId)
  }

  def generateMatchEvent(time: Long, userA: String, userB: String, errorProbability: Double): MatchEvent = {
    val winner = if (random.nextBoolean()) userA else userB
    val gameTier = if (random.nextDouble() < errorProbability) random.nextInt(1000) else 1 + random.nextInt(9)
    val duration = if (random.nextDouble() < errorProbability) random.nextInt(10000) else 30 + random.nextInt(141)
    val userAPostMatchInfo = randomUserPostmatch(errorProbability)
    val userBPostMatchInfo = randomUserPostmatch(errorProbability)
    MatchEvent(EventTypeMatch.`match`, time, userA, userB, userAPostMatchInfo, Some(userBPostMatchInfo), winner, gameTier, duration)
  }

  private def randomUserPostmatch(errorProbability: Double): UserPostmatchInfo =
    UserPostmatchInfo(randomPositiveLongWithError(errorProbability), randomPositiveLongWithError(errorProbability), chooseRandomWithError(devices, errorProbability, randomWrongString()), chooseRandomWithError(platforms, errorProbability, randomWrongString()))
  private def randomPositiveLongWithError(errorProbability: Double, max: Long = 1000): Long =
    if (random.nextDouble() < errorProbability) random.nextLong() * -1 * 200 else 1 + random.nextLong(max)

  private def randomWrongString(length: Int = 5): String =
    Random.alphanumeric.take(length).mkString

  private def chooseRandomWithError[T](seq: IndexedSeq[T], errorProbability: Double, errorValue: => T): T =
    if (random.nextDouble() < errorProbability) errorValue else seq(random.nextInt(seq.length))

  private def chooseRandomWithError[T](list: List[T], errorProbability: Double, errorValue: => T): T = {
    if (random.nextDouble() < errorProbability) errorValue else list(random.nextInt(list.length))
  }
}
