package operations.stream

import com.miniclip.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Try, Success, Failure}

class ValidateEventsTimestamps[T](timestampExtractor: T => Long, invalidIdentifier: String = "invalid")
  extends JoinedStreamOperation[T] with LazyLogging {

  override def apply(stream: KStream[String, (T, InitEvent)]): KStream[String, (T, InitEvent)] = {
    stream.map { (key, value) =>
      val (event, initEvent) = value

      if (initEvent != null) {
        Try(timestampExtractor(event)) match {
          case Success(eventTime) =>
            if (eventTime > initEvent.time) {
              // Valid event, key remains unchanged
              (key, value)
            } else {
              // Invalid event, key set to a special identifier
              logger.debug(s"Invalid event: $event with key: $key")
              (invalidIdentifier, value)
            }
          case Failure(exception) =>
            // Handle any exceptions that occur during timestamp extraction
            logger.error(s"Error extracting timestamp: ${exception.getMessage}")
            (null, value) // Use null or another identifier to denote a failed extraction
        }
      } else {
        // initEvent is null, log and skip the event
        logger.debug(s"Skipping event due to null InitEvent: $event with key: $key")
        (null, value)
      }
    }
  }
}
