package operations

import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.kstream.KStream

class GlobalKTableJoinOperation[K, V, GK, GV, RV](globalTable: GlobalKTable[GK, GV],
                                                  keySelector: (K, V) => GK,
                                                  valueJoiner: (V, GV) => RV) {
  def transformStream: KStream[K, V] => KStream[K, RV] = { stream =>
    stream.leftJoin(globalTable)(
      keySelector,   // Extracts the key for the GlobalKTable from the stream's key and value
      valueJoiner    // How to join the stream's value with the GlobalKTable's value
    )
  }
}