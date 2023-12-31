package common.kafka.utils.stream

import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.streams.scala.kstream.KStream

object KafkaStreamUtils {
  def convertToStringStream[V <: SpecificRecordBase](stream: KStream[String, V]): KStream[String, String] = {
    stream.mapValues(_.toString)
  }
}
