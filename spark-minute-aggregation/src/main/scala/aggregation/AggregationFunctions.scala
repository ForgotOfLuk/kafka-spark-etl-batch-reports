package aggregation

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.{col, count, date_trunc, first, from_unixtime, sum, window}
import scala.util.Try
import com.typesafe.scalalogging.LazyLogging

// Object containing aggregation functions
object AggregationFunctions extends LazyLogging {

  // Aggregates a DataFrame based on specified parameters
  // Utilizes Try for better error handling and returns a Try[DataFrame]
  private def aggregateDataFrame(
    df: DataFrame,
    timeColumn: String,
    groupByColumns: Seq[Column],
    aggregations: Seq[Column],
    selectColumns: Seq[Column]
  ): Try[DataFrame] = Try {
    logger.info(s"Starting aggregation on DataFrame with time column $timeColumn")
    df
      .withColumn("formattedTimestamp", from_unixtime(col(timeColumn)))
      .withWatermark("timestamp", "10 minutes")
      .groupBy(window(col("timestamp"), "1 minute") +: groupByColumns: _*)
      .agg(aggregations.head, aggregations.tail: _*)
      .withColumn("timestamp", date_trunc("minute", col("formattedTimestamp")))
      .select(selectColumns: _*)
  }

  // Aggregates purchase DataFrame and logs the process
  def aggregatePurchaseDF(purchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating purchase DataFrame")
    aggregateDataFrame(
      purchaseDF,
      "time",
      Seq.empty,
      Seq(
        count("*").as("purchaseCount"),
        sum("purchaseValue").as("totalPurchaseValue"),
        first("formattedTimestamp").as("formattedTimestamp")
      ),
      Seq(col("timestamp"), col("totalPurchaseValue"), col("purchaseCount"))
    )
  }

  // Aggregates enriched purchase DataFrame with country-wise revenue
  def aggregateEnrichedPurchaseDF(enrichedPurchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched purchase DataFrame")
    aggregateDataFrame(
      enrichedPurchaseDF,
      "time",
      Seq(col("country")),
      Seq(sum("purchaseValue").as("countryRevenue")),
      Seq(col("timestamp"), col("countryRevenue"))
    )
  }

  // Aggregates enriched match DataFrame with match counts by country
  def aggregateEnrichedMatchDF(enrichedMatchDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched match DataFrame")
    aggregateDataFrame(
      enrichedMatchDF,
      "time",
      Seq(col("country")),
      Seq(count("*").as("matchesByCountry")),
      Seq(col("timestamp"), col("matchesByCountry"))
    )
  }
}
