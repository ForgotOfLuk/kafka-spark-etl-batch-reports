package operations

import com.leca.avro.InitEvent
import com.typesafe.scalalogging.LazyLogging
import common.kafka.utils.globalktable.GlobalKTableJoinOperation
import operations.join.JoinedStreamOperation
import operations.stream.StreamOperation
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.kstream.{Joined, KStream, KTable}
import utils.StreamProcessingUtils.filterValues
import utils.Validation.validateCorrectInitEvents

import scala.language.postfixOps
import scala.util.Try

object StreamEnrichmentOperations extends LazyLogging{

  // Key selector for InitEvent
  private def initEventKeySelector(keySelector: InitEvent => String): (String, InitEvent) => String = (_, event) => keySelector(event)

  private def initEventValueJoiner(valueJoiner: (InitEvent, String) => InitEvent): (InitEvent, String) => InitEvent = (event, name) => {
    Try {
      if (name != null) {
        logger.debug(s"Joining event with table: $name")
        valueJoiner(event, name)
      } else {
        logger.warn(s"No matching keys found for event: $event")
        valueJoiner(event, "Unknown")
      }
    } recover {
      case ex: Exception =>
        logger.error(s"Error processing join for event: $event", ex)
        valueJoiner(event, "Error")
    } get
  }

  private val initEventPlatformKeySelector = initEventKeySelector(initEvent => initEvent.platform)
  private val initEventCountryKeySelector = initEventKeySelector(initEvent => initEvent.country)
  private val initEventPlatformValueJoiner = initEventValueJoiner((initEvent, name) => initEvent.copy(platform = name))
  private val initEventCountryValueJoiner = initEventValueJoiner((initEvent, name) => initEvent.copy(country = name))

  // Transforms a stream with a list of operations
  def transformStream[T](initEventInputStream: KStream[String, T], operations: List[StreamOperation[String, T]]): KStream[String, T] = {
    operations.foldLeft(initEventInputStream) { (stream, operation) =>
      operation.apply(stream)
    }
  }

  // Transforms a joined stream with a list of operations
  def transformJoinedStream[T](joinedStream: KStream[String, (T, InitEvent)], operations: List[JoinedStreamOperation[T]]): KStream[String, T] = {
    operations.foldLeft(joinedStream) { (stream, operation) =>
      operation.apply(stream)
    }.mapValues(_._1)
  }

  // Creates a join operator for GlobalKTable
  private def createJoinOperator(table: GlobalKTable[String, String], keySelector: (String, InitEvent) => String, valueJoiner: (InitEvent, String) => InitEvent): GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent] = {
    new GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent](
      table,
      keySelector,
      valueJoiner
    )
  }

  private def joinInitEventWithPlatform(table: GlobalKTable[String, String]): GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent] =
    createJoinOperator(table, initEventPlatformKeySelector, initEventPlatformValueJoiner)

  private def joinInitEventWithCountry(table: GlobalKTable[String, String]): GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent] =
    createJoinOperator(table, initEventCountryKeySelector, initEventCountryValueJoiner)

  // Enriches the init stream by joining with platforms and countries tables
  def enrichInitStream(initEventInputStream: KStream[String, InitEvent], platformsTable: GlobalKTable[String, String], countriesTable: GlobalKTable[String, String]): (KStream[String, InitEvent], KStream[String, InitEvent]) = {
    // Define the join operations with the GlobalKTables
    val platformJoinOperation: GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent] =
      joinInitEventWithPlatform(platformsTable)

    val countryJoinOperation: GlobalKTableJoinOperation[String, InitEvent, String, String, InitEvent] =
      joinInitEventWithCountry(countriesTable)

    // Apply the join operation to the stream
    val platformOutcome = platformJoinOperation
      .transformStream(initEventInputStream)

    val outcome = countryJoinOperation
      .transformStream(platformOutcome)

    filterValues(outcome, validateCorrectInitEvents)
  }

  def joinInitStream[T](
    stream: KStream[String, T],
    initTable: KTable[String, InitEvent],
    keySerde: Serde[String],
    eventSerde: Serde[T],
    initEventSerde: Serde[InitEvent]
  ): KStream[String, (T, InitEvent)] = {
    // Create Joined instance
    implicit val joined: Joined[String, T, InitEvent] =
      Joined.`with`[String, T, InitEvent](keySerde, eventSerde, initEventSerde)

    // Perform left join
    val leftJoin = stream
      .leftJoin(initTable)((event, initEvent) => {
        (event, initEvent)
      })
    leftJoin
  }
}