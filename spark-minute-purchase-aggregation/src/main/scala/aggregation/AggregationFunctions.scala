package aggregation

import com.typesafe.scalalogging.LazyLogging
import common.utils.AggregationUtils.aggregateDataFrame
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import scala.util.Try

/**
 * Object containing functions for aggregating data in different ways.
 * This includes aggregations for enriched purchase data and user activity data.
 */
object AggregationFunctions extends LazyLogging {

  /**
   * Aggregates enriched purchase DataFrame with country-wise revenue.
   * Calculates the total revenue per country from the enriched purchase data.
   *
   * @param enrichedPurchaseDF DataFrame containing enriched purchase data.
   * @return Try[DataFrame] with aggregated data including country and revenue.
   */
  def aggregateEnrichedPurchaseDF(enrichedPurchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched purchase DataFrame by country")
    aggregateDataFrame(
      enrichedPurchaseDF,
      Seq(col("country")), // Grouping by country
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        col("country"),
        sum("purchaseValue").as("countryRevenue") // Calculating total revenue per country
      ),
      Seq(
        col("timestamp"),
        struct(
          col("country"),
          col("countryRevenue")
        ).as("metadata")
      )
    )
  }

  /**
   * Aggregates purchase data to calculate various metrics within each 1-minute window:
   * - Number of purchases made
   * - Total value of the purchases made
   * - Array of distinct users that made a purchase
   * - Number of distinct users that made a purchase
   *
   * @param purchaseDF DataFrame containing purchase data.
   * @return Try[DataFrame] with aggregated purchase metrics.
   */
  def aggregatePurchaseDF(purchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating purchase DataFrame")
    aggregateDataFrame(
      purchaseDF,
      Seq.empty, // No additional grouping besides time window
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        count("*").as("purchaseCount"),
        sum("purchaseValue").as("totalPurchaseValue"),
        collect_set(col("userId")).as("distinctPurchaseUserIdsArray")
      ),
      Seq(
        col("timestamp"),
        struct(
          col("totalPurchaseValue"),
          col("purchaseCount"),
          col("distinctPurchaseUserIdsArray"),
          size(col("distinctPurchaseUserIdsArray")).as("distinctPurchaseUserIds")
        ).as("metadata")
      )
    )
  }
}
