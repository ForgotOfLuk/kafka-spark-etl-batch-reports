import common.utils.SparkUtils
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class DataFrameTransformationTest extends AnyFlatSpec with BeforeAndAfterAll with SparkUtils {

  var spark: SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    // Set Hadoop home to an empty directory
    System.setProperty("hadoop.home.dir", "/Users/pedrolera/IdeaProjects/Miniclip/spark-daily-aggregation/src/main/resources")

    spark = SparkSession.builder()
      .master("local")
      .appName("DataFrameTransformationTest")
      .config("spark.ui.enabled", "false")
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    if (spark != null) {
      spark.stop()
    }
    super.afterAll()
  }

  it should "transformInitEventDataFrame should transform data correctly" in {
    val data = Seq(
      ("key1", """{"eventType":"type1","time":1622554800000,"userId":"user1","country":"US","platform":"web"}"""),
      ("key2", """{"eventType":"type2","time":1622554801000,"userId":"user2","country":"UK","platform":"mobile"}""")
    )
    val df = spark.createDataFrame(data).toDF("key", "value")

    val transformedDF = transformInitEventDataFrame(df)

    // Assertions
    transformedDF.columns.contains("timestamp") shouldEqual true
    transformedDF.columns.contains("date") shouldEqual true
  }
}
