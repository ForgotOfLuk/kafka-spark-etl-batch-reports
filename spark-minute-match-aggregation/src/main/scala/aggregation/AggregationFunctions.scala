package aggregation

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions._

import scala.util.Try
import com.typesafe.scalalogging.LazyLogging
import common.utils.AggregationUtils.aggregateDataFrame

// Object containing aggregation functions
object AggregationFunctions extends LazyLogging {

  // Aggregates enriched purchase DataFrame with country-wise revenue
  def aggregateEnrichedPurchaseDF(enrichedPurchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched purchase DataFrame")
    aggregateDataFrame(
      enrichedPurchaseDF,
      Seq(col("country")),
      Seq(
        sum("purchaseValue").as("countryRevenue"),
        first("formattedTimestamp").as("formattedTimestamp")
      ),
      Seq(col("timestamp"), col("countryRevenue"))
    )
  }

  // Aggregates enriched match DataFrame with match counts by country
  def aggregateEnrichedMatchDF(enrichedMatchDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating enriched match DataFrame")
    // Explode userACountry and userBCountry into a single column and remove duplicates to get 1 row per country
    val explodedCountryDF = enrichedMatchDF
      .select(col("time"), col("timestamp"), explode(array(col("userACountry"), col("userBCountry"))).as("country"))
      .distinct()

    aggregateDataFrame(
      explodedCountryDF,
      Seq(col("country")),
      Seq(
        countDistinct("country").as("matchesByCountry"),
        first("formattedTimestamp").as("formattedTimestamp")
      ),
      Seq(col("timestamp"), col("matchesByCountry"))
    )
  }
  /**
   * Aggregates user activity data to find distinct users active in each 1-minute window.
   *
   * @param transformedMatchDF DataFrame containing match data.
   * @param transformedPurchaseDF DataFrame containing purchase data.
   * @return Try[DataFrame] with columns "timestamp" and "distinctUserIds".
   */


  def getUsersByTimeDF(transformedMatchDF: DataFrame, transformedPurchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Starting to enrich Purchase DataFrame with Match DataFrame to know every user by time.")

    aggregateDataFrame(
      transformedMatchDF,
      Seq.empty,
      Seq(
        size(array_distinct(flatten(collect_list(array(col("userA"), col("userB")).as("userIds"))))).as("distinctUserIds"),
        first("formattedTimestamp").as("formattedTimestamp")
      ),
      Seq(col("timestamp"), col("distinctUserIds"))
    )
  }

  // Aggregates purchase DataFrame and logs the process
  def aggregatePurchaseDF(purchaseDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating purchase DataFrame")
    aggregateDataFrame(
      purchaseDF,
      Seq.empty,
      Seq(
        count("*").as("purchaseCount"),
        sum("purchaseValue").as("totalPurchaseValue"),
        first("formattedTimestamp").as("formattedTimestamp")
      ),
      Seq(col("timestamp"), col("totalPurchaseValue"), col("purchaseCount"))
    )
  }
}
