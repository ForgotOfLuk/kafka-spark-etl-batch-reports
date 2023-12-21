package operations.stream

import com.miniclip.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream

class ValidateEventsTimestamps[T](timestampExtractor: T => Long, invalidIdentifier: String = "invalid") extends JoinedStreamOperation[T] {
  override def apply(stream: KStream[String, (T, InitEvent)]): KStream[String, (T, InitEvent)] = {
    stream.map { (key, value) =>
      val (event, initEvent) = value
      if(initEvent != null) { // if its null, ignore it
        val eventTime = timestampExtractor(event)
        if (eventTime > initEvent.time) {
          (key, value) // Valid event, key remains unchanged
        } else {
          (invalidIdentifier, value) // Invalid event, key set to a special identifier
        }
      } else {
        (null, value)
      }
    }
  }
}