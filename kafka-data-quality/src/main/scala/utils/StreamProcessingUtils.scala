package utils

import com.miniclip.avro.{InitEvent, MatchEvent}
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.scala.kstream.{Branched, KStream, KTable}

object StreamProcessingUtils extends LazyLogging {

  def createInitEventKTable(stream: KStream[String, InitEvent]): KTable[String, InitEvent] = {
    logger.info("Creating KTable from InitEvent stream")
    stream.toTable
  }
  def getValueBranches[T](name: String, input: KStream[String, T], separateFunction: T => Boolean): (KStream[String, T], KStream[String, T]) = {
    val branches = input.split(Named.as(s"Split-$name"))
      .branch((_, value) => separateFunction(value))
      .branch((_, value) => !separateFunction(value))
      .noDefaultBranch()

    val mainOutput = branches.head._2
    val sideOutput = branches.last._2

    (mainOutput, sideOutput)
  }

  def getKeyBranches[T](name: String, input: KStream[String, T], separateFunction: String => Boolean): (KStream[String, T], KStream[String, T]) = {
    val branches = input.split(Named.as(s"Split-$name"))
      .branch((key, _) => separateFunction(key))
      .branch((key, _) => !separateFunction(key))
      .noDefaultBranch()

    val mainOutput = branches.head._2
    val sideOutput = branches.last._2

    (mainOutput, sideOutput)
  }

  // separateFunction to check if key is null or "invalid"
  def separateFunction: String => Boolean = key => key != null && key != "invalid"

}