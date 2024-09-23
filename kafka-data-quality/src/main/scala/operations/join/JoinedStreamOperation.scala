package operations.join

import com.leca.avro.InitEvent
import org.apache.kafka.streams.scala.kstream.KStream

trait JoinedStreamOperation[T] {
  def apply(stream: KStream[String, (T, InitEvent)]): KStream[String, (T, InitEvent)]
}
