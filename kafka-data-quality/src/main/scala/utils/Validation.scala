package utils

import cats.data.Validated
import com.miniclip.avro.InitEvent
import com.typesafe.scalalogging.LazyLogging

object Validation extends LazyLogging {

  // Validates InitEvent objects by checking specific fields
  def validateCorrectInitEvents(initEvent: InitEvent): Validated[String, InitEvent] = {
    validateCorrectValue(initEvent, initEvent.country)
      .andThen(v => validateCorrectValue(v, v.platform))
      .leftMap { errMsg =>
        logger.error(s"Validation failed for InitEvent: $errMsg")
        errMsg
      }
  }

  // Validates that the given key is neither null nor 'invalid'
  def separateFunction: String => Validated[String, String] = key =>
    if (key != null && key != "invalid") Validated.valid(key)
    else {
      logger.debug("Received invalid or null key")
      Validated.invalid("Invalid or null key")
    }

  // Validates a given key value for a given event
  private def validateCorrectValue[T](event: T, key: String): Validated[String, T] = {
    if (key != "Unknown" && key != "Error") Validated.valid(event)
    else {
      logger.debug(s"Invalid key content detected: $key")
      Validated.invalid("Invalid key content")
    }
  }
}
