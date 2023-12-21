package operations.stream

import org.apache.kafka.streams.scala.kstream.KStream

trait StreamOperation[K, V] {
  def apply(stream: KStream[K, V]): KStream[K, V]
}
