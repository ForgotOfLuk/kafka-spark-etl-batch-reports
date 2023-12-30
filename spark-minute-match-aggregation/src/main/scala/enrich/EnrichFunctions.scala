package enrich

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import scala.util.Try

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

    // Format the initial DataFrame and remove unnecessary fields
    val formattedInitDF = removeFields(transformedInitDF)

    // Rename the 'userId' column in transformedInitDF to avoid ambiguity during the join
    val renamedInitDF = formattedInitDF.withColumnRenamed("userId", "initUserId")

    // Join the transformedMatchDF with renamedInitDF on userA
    // The join condition now uses 'initUserId' to match with 'userA' from the match DataFrame
    val matchWithUserACountry = transformedMatchDF
      .join(renamedInitDF.withColumnRenamed("country", "userACountry"), col("userA") === col("initUserId"), "left")
      .drop("initUserId") // Drop the 'initUserId' column after the join

    // Perform a similar join for userB
    // Here too, 'initUserId' is used to match with 'userB' from the match DataFrame
    val enrichedMatchDF = matchWithUserACountry
      .join(renamedInitDF.withColumnRenamed("country", "userBCountry"), col("userB") === col("initUserId"), "left")
      .drop("initUserId") // Drop the 'initUserId' column after the join

    enrichedMatchDF
  }


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
      .join(removeFields(transformedInitDF), Seq("userId"), "left")
  }

  private def removeFields(transformedInitDF: DataFrame) = {
    transformedInitDF
      .drop("time")
      .drop("timestamp")
      .drop("eventType")
      .drop("date")
  }
}
