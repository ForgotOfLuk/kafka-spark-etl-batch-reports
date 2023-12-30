package enrich

import com.typesafe.scalalogging.LazyLogging
import common.utils.EnrichUtils.removeInitFieldsForEnrichment
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import scala.util.Try

/**
 * Object containing functions to enrich DataFrames with additional information,
 * particularly for enriching match data with user-related information.
 */
object EnrichFunctions extends LazyLogging {

  /**
   * Enriches match DataFrame with country information of users.
   * This function takes initial user data and match data, and adds country information
   * for each user involved in the matches.
   *
   * @param transformedInitDF DataFrame containing initial user data.
   * @param transformedMatchDF DataFrame containing match data.
   * @return DataFrame enriched with user country data.
   */
  def enrichMatchDF(transformedInitDF: DataFrame, transformedMatchDF: DataFrame): Try[DataFrame] = Try {
    logger.info("Starting to enrich Match DataFrame with user country information.")

    // Format the initial DataFrame to prepare it for enrichment and remove unnecessary fields.
    val formattedInitDF = removeInitFieldsForEnrichment(transformedInitDF)
    logger.debug("Formatted initial DataFrame and removed unnecessary fields for enrichment.")

    // Rename the 'userId' column in transformedInitDF to 'initUserId' to avoid ambiguity during the join.
    val renamedInitDF = formattedInitDF.withColumnRenamed("userId", "initUserId")
    logger.debug("Renamed 'userId' to 'initUserId' in the initial DataFrame to avoid ambiguity.")

    // Join the transformedMatchDF with renamedInitDF on 'userA' and add 'userACountry'.
    // The join condition uses 'initUserId' to match with 'userA' from the match DataFrame.
    val matchWithUserACountry = transformedMatchDF
      .join(renamedInitDF.withColumnRenamed("country", "userACountry"), col("userA") === col("initUserId"), "left")
      .drop("initUserId") // Drop the 'initUserId' column after the join
    logger.debug("Joined match data with userA's country information.")

    // Perform a similar join for 'userB' to add 'userBCountry'.
    // Again, 'initUserId' is used to match with 'userB' from the match DataFrame.
    val enrichedMatchDF = matchWithUserACountry
      .join(renamedInitDF.withColumnRenamed("country", "userBCountry"), col("userB") === col("initUserId"), "left")
      .drop("initUserId") // Drop the 'initUserId' column after the join
    logger.debug("Joined match data with userB's country information.")

    enrichedMatchDF
  }
}
