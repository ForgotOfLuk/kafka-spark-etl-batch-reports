package common.utils

import org.apache.spark.sql.DataFrame

object EnrichUtils {
  def removeInitFieldsForEnrichment(transformedInitDF: DataFrame): DataFrame = {
    transformedInitDF.drop("time", "timestamp", "eventType", "date")
  }
}
