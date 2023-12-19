import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.EventGenerator

class EventGeneratorTest extends AnyFlatSpec with Matchers {

  "EventGenerator" should "generate a InitEvent" in {
    val initEvent = EventGenerator.generateInitEvent(System.currentTimeMillis(), "user123", 0.0)
    initEvent should not be null
    initEvent.userId shouldBe "user123"
    initEvent.eventType shouldBe com.miniclip.avro.EventTypeInit.init
  }

  it should "generates a InAppPurchaseEvent" in {
    val inAppPurchaseEvent = EventGenerator.generateInAppPurchaseEvent(System.currentTimeMillis(), "user123", 0.0)
    inAppPurchaseEvent should not be null
    inAppPurchaseEvent.userId shouldBe "user123"
    inAppPurchaseEvent.eventType shouldBe com.miniclip.avro.EventTypeInAppPurchase.in_app_purchase
  }

  it should "generate a MatchEvent" in {
    val matchEvent = EventGenerator.generateMatchEvent(System.currentTimeMillis(), "userA", "userB", 0.0)
    matchEvent should not be null
    assert(matchEvent.userA == "userA" || matchEvent.userB == "userB", "Winner should be either userA or userB")
    matchEvent.eventType shouldBe com.miniclip.avro.EventTypeMatch.`match`
  }
}
