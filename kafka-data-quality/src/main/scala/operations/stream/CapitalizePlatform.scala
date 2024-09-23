package operations.stream

import com.leca.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Try, Success, Failure}

object CapitalizePlatform extends StreamOperation[String, InitEvent] with LazyLogging {
  override def apply(stream: KStream[String, InitEvent]): KStream[String, InitEvent] = {
    stream.mapValues { event =>
      Try {
        val capitalizedPlatform = event.platform.capitalize
        event.copy(platform = capitalizedPlatform)
      } match {
        case Success(updatedEvent) =>
          updatedEvent
        case Failure(exception) =>
          logger.error(s"Error capitalizing platform for event: ${exception.getMessage}")
          event // Return the original event in case of failure
      }
    }
  }
}
