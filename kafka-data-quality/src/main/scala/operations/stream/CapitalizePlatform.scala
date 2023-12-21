package operations.stream

import com.miniclip.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream

object CapitalizePlatform extends StreamOperation[String, InitEvent] {
  override def apply(stream: KStream[String, InitEvent]): KStream[String, InitEvent] = {
    stream.mapValues { event =>
      val capitalizedPlatform = event.platform.capitalize
      event.copy(platform = capitalizedPlatform)
    }
  }
}
