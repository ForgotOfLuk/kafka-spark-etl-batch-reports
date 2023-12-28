package utils

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, countDistinct, date_trunc, struct}

import scala.util.Try

object PipelineUtils extends LazyLogging{
  //Aggregate data and transform to correct format for mongoDb
  def aggregateData(df: DataFrame): DataFrame = Try {
    logger.info("Aggregating Data")
    // Perform the aggregation
    df
      .withColumn("day", date_trunc("day", col("date"))).as("day")
      .groupBy(
        col("day"),
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
}
