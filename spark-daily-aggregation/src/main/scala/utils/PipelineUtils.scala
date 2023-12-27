package utils

import com.typesafe.scalalogging.LazyLogging
import common.model.MongoConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, countDistinct, date_trunc, struct}

import scala.util.Try

object PipelineUtils extends LazyLogging{
  //Aggregate data and transform to correct format for mongoDb
  def aggregateData(df: DataFrame): DataFrame = Try {
    logger.info("Aggregating Data")
    // Perform the aggregation
    df
      .withColumn("day", date_trunc("day", col("timestamp")))
      .groupBy(
        date_trunc("day", col("timestamp")).as("day"),
        col("country"),
        col("platform")
      )
      .agg(countDistinct("userId").as("numberOfUsers"))
      .withColumn("userData", struct(col("numberOfUsers"), col("country"), col("platform")))
      .select(col("day").as("timestamp"), col("userData"))
  }.getOrElse {
    logger.error("Aggregation failed")
    throw new RuntimeException("Failed to aggregate DataFrame with watermark")
  }

  def getStartEndPointConfig(mongoConfig: MongoConfig): Map[String, String] = {
    val timestampOption = MongoUtils(mongoConfig).retrieveLatestTimestamp()
    if (timestampOption.isDefined) {
      Map("startingTimestamp" -> timestampOption.get, "endingTimestamp" -> System.currentTimeMillis().toString)
    } else {
      Map("startingOffsets" -> "earliest", "endingOffsets" -> "latest") // Default to earliest if no timestamp found
    }
  }
}
