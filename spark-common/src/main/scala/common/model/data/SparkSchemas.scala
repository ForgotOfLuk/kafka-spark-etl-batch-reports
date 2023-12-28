package common.model.data

import org.apache.spark.sql.types.{LongType, StringType, StructType, DoubleType, IntegerType}

object SparkSchemas {
  // Schema definitions

  def initEventSchema: StructType = new StructType()
    .add("eventType", StringType, nullable = true)
    .add("time", LongType, nullable = true)
    .add("userId", StringType, nullable = true)
    .add("country", StringType, nullable = true)
    .add("platform", StringType, nullable = true)

  def purchaseEventSchema: StructType = new StructType()
    .add("eventType", StringType, nullable = true)
    .add("time", LongType, nullable = true)
    .add("purchaseValue", DoubleType, nullable = true)
    .add("userId", StringType, nullable = true)
    .add("productId", StringType, nullable = true)

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
  }
}
