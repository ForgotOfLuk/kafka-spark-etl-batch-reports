package utils

import cats.data.Validated
import com.miniclip.avro.InitEvent
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}

object StreamProcessingUtils extends LazyLogging {

  def createInitEventKTable(stream: KStream[String, InitEvent]): KTable[String, InitEvent] = {
    logger.info("Creating KTable from InitEvent stream")
    stream.toTable
  }
  def filterValues[T](input: KStream[String, T], validationFunction: T => Validated[String, T]): (KStream[String, T], KStream[String, T]) = {
    val mainOutput = input.filter((_, value) => validationFunction(value).isValid)
    val sideOutput = input.filter((_, value) => validationFunction(value).isInvalid)

    (mainOutput, sideOutput)
  }

  def filterKeys[T](input: KStream[String, T], validationFunction: String => Validated[String, String]): (KStream[String, T], KStream[String, T]) = {
    val mainOutput = input.filter((key, _) => validationFunction(key).isValid)
    val sideOutput = input.filter((key, _) => validationFunction(key).isInvalid)

    (mainOutput, sideOutput)
  }


}