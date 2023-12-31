package common.utils

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame}

import scala.util.Try

// Object containing aggregation functions
object AggregationUtils extends LazyLogging {

  // Aggregates a DataFrame based on specified parameters
  // Utilizes Try for better error handling and returns a Try[DataFrame]
  def aggregateDataFrame(
    df: DataFrame,
    groupByColumns: Seq[Column],
    aggregations: Seq[Column],
    selectColumns: Seq[Column]
  ): Try[DataFrame] = Try {
    logger.info(s"Starting aggregation on DataFrame with time column")
    df
      .withColumn("formattedTimestamp", from_unixtime(col("time") / 1000))
      .withWatermark("timestamp", "10 minutes")
      .groupBy(window(col("timestamp"), "1 minute") +: groupByColumns: _*)
      .agg(aggregations.head, aggregations.tail: _*)
      .withColumn("timestamp", date_trunc("minute", col("formattedTimestamp")))
      .select(selectColumns: _*)
  }
}
