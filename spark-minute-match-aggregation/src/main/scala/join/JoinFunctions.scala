package join

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, struct}
import scala.util.Try
import com.typesafe.scalalogging.LazyLogging

/**
 * Object containing functions to join DataFrames.
 */
object JoinFunctions extends LazyLogging {

  /**
   * Joins multiple DataFrames based on a common timestamp field.
   * @param aggregatedPurchaseDF DataFrame containing aggregated purchase data.
   * @param aggregatedInitDF DataFrame containing aggregated initialization data.
   * @param aggregatedEnrichedMatchDF DataFrame containing aggregated enriched match data.
   * @return DataFrame resulting from the join of the input DataFrames.
   */
  def joinDfs(aggregatedPurchaseDF: DataFrame, distinctUserInfo: DataFrame, aggregatedEnrichedMatchDF: DataFrame, aggregatedEnrichedPurchaseDF: DataFrame): Try[DataFrame] = Try {
    logger.info("Starting to join DataFrames based on the timestamp field.")

    // Full outer join on the timestamp field to ensure no data is missed
    val joinedDF = aggregatedPurchaseDF
      .join(distinctUserInfo, Seq("timestamp"), "outer")
      .join(aggregatedEnrichedMatchDF, Seq("timestamp"), "outer")
      .join(aggregatedEnrichedPurchaseDF, Seq("timestamp"), "outer")
      .select(
        col("timestamp"),
        struct(
          col("purchaseCount"),
          col("totalPurchaseValue"),
          col("distinctUserIds"),
          col("countryRevenue"),
          col("matchesByCountry")
        ).as("userData")
      )

    logger.info("DataFrames joined successfully.")
    joinedDF
  }
}
