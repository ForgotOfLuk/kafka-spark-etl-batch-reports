package enrich

import com.typesafe.scalalogging.LazyLogging
import common.utils.EnrichUtils.removeInitFieldsForEnrichment
import org.apache.spark.sql.DataFrame

import scala.util.Try

/**
 * Object containing functions to enrich DataFrames with additional information.
 */
object EnrichFunctions extends LazyLogging {

  /**
   * Enriches purchase DataFrame with user information.
   *
   * @param transformedInitDF DataFrame containing initial user data.
   * @param transformedPurchaseDF DataFrame containing purchase data.
   * @return DataFrame enriched with user information.
   */
  def enrichPurchaseDF(transformedInitDF: DataFrame, transformedPurchaseDF: DataFrame): Try[DataFrame] = Try {
    logger.info("Starting to enrich Purchase DataFrame with user information.")
    transformedPurchaseDF
      //drops time column since the time of the init is irrelevant for the future operations
      .join(removeInitFieldsForEnrichment(transformedInitDF), Seq("userId"), "left")
  }

}
