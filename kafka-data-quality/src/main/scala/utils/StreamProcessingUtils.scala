package utils

import cats.data.Validated
import com.miniclip.avro.InitEvent
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}

object StreamProcessingUtils extends LazyLogging {

  // Method to create a KTable from a KStream of InitEvent
  def createInitEventKTable(stream: KStream[String, InitEvent]): KTable[String, InitEvent] = {
    logger.info("Creating KTable from InitEvent stream")
    stream.toTable
  }

  // Method to filter a KStream based on a validation function applied to either key or value
  private def filterStream[K, V, A](input: KStream[K, V], validationFunction: A => Validated[String, A], selector: ((K, V)) => A): (KStream[K, V], KStream[K, V]) = {
    val mainOutput = input.filter { case (key, value) =>
      val toValidate = selector((key, value))
      val result = validationFunction(toValidate)
      if (result.isInvalid) logger.debug(s"Failed validation: $toValidate")
      result.isValid
    }
    val sideOutput = input.filter { case (key, value) =>
      validationFunction(selector((key, value))).isInvalid
    }

    (mainOutput, sideOutput)
  }

  // Wrapper method for filtering based on keys
  def filterKeys[K, V](input: KStream[K, V], validationFunction: K => Validated[String, K]): (KStream[K, V], KStream[K, V]) = {
    filterStream(input, validationFunction, _._1)
  }

  // Wrapper method for filtering based on values
  def filterValues[K, V](input: KStream[K, V], validationFunction: V => Validated[String, V]): (KStream[K, V], KStream[K, V]) = {
    filterStream(input, validationFunction, _._2)
  }
}
