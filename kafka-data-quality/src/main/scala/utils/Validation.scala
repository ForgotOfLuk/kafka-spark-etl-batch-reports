package utils

import cats.data.Validated
import com.miniclip.avro.InitEvent

object Validation {

  def validateCorrectInitEvents(initEvent: InitEvent): Validated[String, InitEvent] = {
    validateCorrectValue(initEvent, initEvent.country)
      .andThen(v => validateCorrectValue(v, v.platform))
  }
  def separateFunction: String => Validated[String, String] = key =>
    if (key != null && key != "invalid") Validated.valid(key)
    else Validated.invalid("Invalid or null key")

  private def validateCorrectValue[T](event: T, key: String): Validated[String, T] = {
    if (key != "Unknown" && key != "Error") Validated.valid(event)
    else Validated.invalid("Invalid key content")
  }

}
