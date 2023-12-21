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
    val branchedStream = input.split(Named.as(s"Split-$name"))
      .branch((_, value) => separateFunction(value), Branched.as("Main"))
      .defaultBranch(Branched.as("Side"))

    val mainBranch = branchedStream.get("Main")
    val sideBranch = branchedStream.get("Side")

    (mainBranch.orNull, sideBranch.orNull)
  }

  def getKeyBranches[T](name: String, input: KStream[String, T], separateFunction: String => Boolean): (KStream[String, T], KStream[String, T]) = {
    val branchedStream = input.split(Named.as(s"Split-$name"))
      .branch((key, _) => separateFunction(key), Branched.as("Main"))
      .defaultBranch(Branched.as("Side"))

    val mainBranch = branchedStream.get("Main")
    val sideBranch = branchedStream.get("Side")

    (mainBranch.orNull, sideBranch.orNull)
  }

  // separateFunction to check if key is null or "invalid"
  def separateFunction: String => Boolean = key => key != null && key != "invalid"

}