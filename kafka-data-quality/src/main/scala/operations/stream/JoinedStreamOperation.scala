package operations.stream

import com.miniclip.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream

trait JoinedStreamOperation[T] {
  def apply(stream: KStream[String, (T, InitEvent)]): KStream[String, (T, InitEvent)]
}
