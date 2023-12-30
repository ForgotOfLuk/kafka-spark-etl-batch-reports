package aggregation

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.{array_distinct, _}

import scala.util.Try
import com.typesafe.scalalogging.LazyLogging
import common.utils.AggregationUtils.aggregateDataFrame

// Object containing aggregation functions
object AggregationFunctions extends LazyLogging {

  // Aggregates enriched purchase DataFrame with country-wise revenue
  def aggregateEnrichedPurchaseDF(enrichedPurchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched purchase DataFrame")
    aggregateDataFrame(
      //dataframe
      enrichedPurchaseDF,
      //aggregations other than time window
      Seq(col("country")),
      //aggregation functions
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        col("country"),
        sum("purchaseValue").as("countryRevenue")
      ),
      //select
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
   * Aggregates user activity data to find in each 1-minute window
   *  - number of purchases made
   *  - total value of the purchases made
   *  - distinct users that made a purchase
   *  - number of distinct users that made a purchase
   *
   * @param purchaseDF DataFrame containing match data.
   * @return Try[DataFrame] with columns "timestamp" and "distinctUserIds".
   */

  // Aggregates purchase DataFrame and logs the process
  def aggregatePurchaseDF(purchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating purchase DataFrame")
    aggregateDataFrame(
      //dataframe
      purchaseDF,
      //aggregations other than time window
      Seq.empty,
      //aggregation functions
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        count("*").as("purchaseCount"),
        sum("purchaseValue").as("totalPurchaseValue"),
        collect_set(col("userId")).as("distinctPurchaseUserIdsArray"),

      ),
      //select
      Seq(
        col("timestamp"),
        struct(
          col("totalPurchaseValue"),
          col("purchaseCount"),
          col("distinctPurchaseUserIdsArray"), //keep this information for future reference
          size(col("distinctPurchaseUserIdsArray")).as("distinctPurchaseUserIds")
        ).as("metadata")
      )
    )
  }
}
