package enrich

import org.apache.spark.sql.DataFrame
import scala.util.Try
import com.typesafe.scalalogging.LazyLogging

/**
 * Object containing functions to enrich DataFrames with additional information.
 */
object EnrichFunctions extends LazyLogging {

  /**
   * Enriches match DataFrame with country information of users.
   * @param transformedInitDF DataFrame containing initial user data.
   * @param transformedMatchDF DataFrame containing match data.
   * @return DataFrame enriched with user country data.
   */
  def enrichMatchDF(transformedInitDF: DataFrame, transformedMatchDF: DataFrame): Try[DataFrame] = Try {
    logger.info("Starting to enrich Match DataFrame with user country information.")
    val matchWithUserACountry = transformedMatchDF
      .join(transformedInitDF.withColumnRenamed("country", "userACountry"), transformedMatchDF("userA") === transformedInitDF("userId"), "left")
      .drop(transformedInitDF("userId"))

    matchWithUserACountry
      .join(transformedInitDF.withColumnRenamed("country", "userBCountry"), matchWithUserACountry("userB") === transformedInitDF("userId"), "left")
      .drop(transformedInitDF("userId"))
  }

  /**
   * Enriches purchase DataFrame with user information.
   * @param transformedInitDF DataFrame containing initial user data.
   * @param transformedPurchaseDF DataFrame containing purchase data.
   * @return DataFrame enriched with user information.
   */
  def enrichPurchaseDF(transformedInitDF: DataFrame, transformedPurchaseDF: DataFrame): Try[DataFrame] = Try {
    logger.info("Starting to enrich Purchase DataFrame with user information.")
    transformedPurchaseDF
      .join(transformedInitDF, Seq("userId"), "left")
  }
}
