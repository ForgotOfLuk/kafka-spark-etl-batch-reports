package aggregation

import com.typesafe.scalalogging.LazyLogging
import common.utils.AggregationUtils.aggregateDataFrame
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import scala.util.Try

// Object for performing various aggregation functions on DataFrames.
object AggregationFunctions extends LazyLogging {

  /**
   * Aggregates enriched match DataFrame by counting matches per country.
   *
   * @param enrichedMatchDF DataFrame containing enriched match data.
   * @return A Try[DataFrame] with aggregated match counts by country.
   */
  def aggregateEnrichedMatchDF(enrichedMatchDF: DataFrame): Try[DataFrame] = {
    logger.info("Starting aggregation of enriched match DataFrame by country.")

    // Explode userACountry and userBCountry into a single column 'country' and remove duplicates.
    val explodedCountryDF = enrichedMatchDF
      .select(
        col("time"),
        col("timestamp"),
        explode(array(col("countryA"), col("countryB"))).as("country")
      ).distinct()

    logger.info("Exploded user countries and removed duplicates for aggregation.")

    // Perform the aggregation using a helper function.
    aggregateDataFrame(
      explodedCountryDF,
      Seq(col("country")),
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        col("country"),
        count("*").as("matchesByCountry"),
      ),
      Seq(
        col("timestamp"),
        struct(
          col("country"),
          col("matchesByCountry")
        ).as("metadata")
      )
    )
  }

  /**
   * Aggregates user activity data to find distinct users active in each 1-minute window.
   *
   * @param transformedMatchDF DataFrame containing match data.
   * @return Try[DataFrame] with columns "timestamp" and "distinctMatchUserIdsArray".
   */
  def getUsersByTimeDF(transformedMatchDF: DataFrame): Try[DataFrame] = {
    logger.info("Aggregating match data to find distinct users active in each 1-minute window.")

    // Perform aggregation to find distinct users in each 1-minute window.
    aggregateDataFrame(
      transformedMatchDF,
      Seq.empty,
      Seq(
        first("formattedTimestamp").as("formattedTimestamp"),
        array_distinct(flatten(collect_list(array(col("userA"), col("userB")).as("userIds")))).as("distinctMatchUserIdsArray")
      ),
      Seq(
        col("timestamp"),
        struct(
          size(col("distinctMatchUserIdsArray")).as("distinctMatchUserIds"),
          col("distinctMatchUserIdsArray")
        ).as("metadata")
      )
    )
  }
}
