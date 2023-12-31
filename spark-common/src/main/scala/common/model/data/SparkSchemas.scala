package common.model.data

import org.apache.spark.sql.types.{LongType, StringType, StructType, DoubleType, IntegerType}

/**
 * Object to define Spark schemas for various event types.
 */
object SparkSchemas {

  /**
   * Defines the schema for initialization events.
   * @return StructType representing the schema.
   */
  def initEventSchema: StructType = new StructType()
    .add("eventType", StringType, nullable = true)
    .add("time", LongType, nullable = true)
    .add("userId", StringType, nullable = true)
    .add("country", StringType, nullable = true)
    .add("platform", StringType, nullable = true)

  /**
   * Defines the schema for purchase events.
   * @return StructType representing the schema.
   */
  def purchaseEventSchema: StructType = new StructType()
    .add("eventType", StringType, nullable = true)
    .add("time", LongType, nullable = true)
    .add("purchaseValue", DoubleType, nullable = true)
    .add("userId", StringType, nullable = true)
    .add("productId", StringType, nullable = true)
    .add("country", StringType, nullable = true)

  /**
   * Defines the schema for match events, including nested post-match information.
   * @return StructType representing the schema.
   */
  def matchEventSchema: StructType = {
    val postmatchInfoSchema = new StructType()
      .add("coinBalanceAfterMatch", IntegerType, nullable = true)
      .add("levelAfterMatch", IntegerType, nullable = true)
      .add("device", StringType, nullable = true)
      .add("platform", StringType, nullable = true)

    new StructType()
      .add("eventType", StringType, nullable = true)
      .add("time", LongType, nullable = true)
      .add("userA", StringType, nullable = true)
      .add("userB", StringType, nullable = true)
      .add("userAPostmatchInfo", postmatchInfoSchema)
      .add("userBPostmatchInfo", postmatchInfoSchema, nullable = true)
      .add("winner", StringType, nullable = true)
      .add("gameTier", IntegerType, nullable = true)
      .add("duration", IntegerType, nullable = true)
      .add("countryA", StringType, nullable = true)
      .add("countryB", StringType, nullable = true)
  }
}
