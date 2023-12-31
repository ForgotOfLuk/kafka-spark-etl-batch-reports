package operations

import com.miniclip.avro.{EnrichedInAppPurchaseEvent, EnrichedMatchEvent, InAppPurchaseEvent, InitEvent, MatchEvent}
import common.kafka.utils.globalktable.GlobalKTableJoinOperation
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.kstream.KStream

import scala.util.{Failure, Success, Try}

object StreamEnrichmentOperations extends LazyLogging {

  /**
   * Enriches MatchEvent stream with country information from InitEvent for both userA and userB.
   *
   * @param matchStream KStream of MatchEvent.
   * @param initTable   GlobalKTable of InitEvent keyed by userId.
   * @return Tuple of KStreams - enriched MatchEvent stream and unmatched MatchEvent stream.
   */
  def enrichMatchStream(matchStream: KStream[String, MatchEvent], initTable: GlobalKTable[String, InitEvent]): (KStream[String, EnrichedMatchEvent], KStream[String, MatchEvent]) = {
    logger.info("Starting enrichment of MatchEvent stream.")

    Try {
      // First join for userA
      val joinUserA = new GlobalKTableJoinOperation[String, MatchEvent, String, InitEvent, (MatchEvent, Option[InitEvent])](
        initTable,
        (_, value) => value.userA,
        (matchEvent, initEvent) => (matchEvent, Option(initEvent))
      )

      // Intermediate stream
      val intermediateStream = joinUserA.transformStream(matchStream)

      // Second join for userB
      val joinUserB = new GlobalKTableJoinOperation[String, (MatchEvent, Option[InitEvent]), String, InitEvent, (MatchEvent, EnrichedMatchEvent)](
        initTable,
        (_, value) => value._1.userB,
        (pair, userBInit) => {
          val (matchEvent, userAInit) = pair
          val enrichedEvent = EnrichedMatchEvent(
            matchEvent.eventType,
            matchEvent.time,
            matchEvent.userA,
            matchEvent.userB,
            matchEvent.userAPostmatchInfo,
            matchEvent.userBPostmatchInfo,
            matchEvent.winner,
            matchEvent.gameTier,
            matchEvent.duration,
            userAInit.map(_.country).getOrElse(""),
            Option(userBInit).map(_.country).getOrElse("")
          )
          (matchEvent, enrichedEvent)
        }
      )

      // Perform the second join
      val enrichedStreamWithOriginal = joinUserB.transformStream(intermediateStream)

      // Split into matched and unmatched streams
      val (matchedStream, unmatchedStream) = {
        val matched = enrichedStreamWithOriginal.filter((_, value) => value._2.countryA.nonEmpty || value._2.countryB.nonEmpty).mapValues(_._2)
        val unmatched = enrichedStreamWithOriginal.filter((_, value) => value._2.countryA.isEmpty && value._2.countryB.isEmpty).mapValues(_._1)
        (matched, unmatched)
      }

      logger.info("MatchEvent stream enrichment completed successfully.")
      (matchedStream, unmatchedStream)
    } match {
      case Success((matchedStream, unmatchedStream)) =>
        logger.info("MatchEvent stream enrichment completed successfully.")
        (matchedStream, unmatchedStream)
      case Failure(e) =>
        logger.error("Error encountered during MatchEvent stream enrichment: ", e)
        throw e //in case of exceptions i want to stop the streams and analise the issue.
    }
  }

  /**
   * Enriches InAppPurchaseEvent stream with country information from InitEvent.
   *
   * @param purchaseStream KStream of InAppPurchaseEvent.
   * @param initTable      GlobalKTable of InitEvent keyed by userId.
   * @return Tuple of KStreams - enriched InAppPurchaseEvent stream and unmatched InAppPurchaseEvent stream.
   */
  def enrichInAppPurchaseStream(purchaseStream: KStream[String, InAppPurchaseEvent], initTable: GlobalKTable[String, InitEvent]): (KStream[String, EnrichedInAppPurchaseEvent], KStream[String, InAppPurchaseEvent]) = {
    Try {
      val joinOperation = new GlobalKTableJoinOperation[String, InAppPurchaseEvent, String, InitEvent, EnrichedInAppPurchaseEvent](
        initTable,
        (_, purchaseEvent) => purchaseEvent.userId, // Key selector for userId
        (purchaseEvent, initEvent) => // Value joiner
          if (initEvent != null) {
            EnrichedInAppPurchaseEvent(
              purchaseEvent.eventType,
              purchaseEvent.time,
              purchaseEvent.purchaseValue,
              purchaseEvent.userId,
              purchaseEvent.productId,
              initEvent.country // Enrich with country
            )
          } else null
      )

      val enrichedStream = joinOperation.transformStream(purchaseStream)

      // Separating matched and unmatched events
      val unmatchedStream: KStream[String, InAppPurchaseEvent] = enrichedStream
        .filter((_, value) => value == null)
        .mapValues((_, value) => value.asInstanceOf[InAppPurchaseEvent])

      (enrichedStream.filter((_, value) => value != null), unmatchedStream)
    } match {
      case Success((matchedStream, unmatchedStream)) =>
        logger.info("inAppPurchase stream enrichment completed successfully.")
        (matchedStream, unmatchedStream)
      case Failure(e) =>
        logger.error("Error encountered during inAppPurchase stream enrichment: ", e)
        throw e //in case of exceptions i want to stop the streams and analise the issue.
    }
  }
}
