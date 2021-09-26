package Domain

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo, JsonTypeName}

import Domain.Entities._
import Domain.JsonSerializable

object Intents {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[Yes], name = "yes"),
      new JsonSubTypes.Type(value = classOf[No], name = "no"),
      new JsonSubTypes.Type(value = classOf[Number], name = "number"),
      new JsonSubTypes.Type(value = classOf[UselessIntent], name = "useless")
    )
  )
  sealed trait Intent extends JsonSerializable
  sealed trait BoolAnswer[T] extends Intent
  final case class Yes(recognizedText: RecognizedText) extends BoolAnswer[Yes]
  final case class No(recognizedText: RecognizedText) extends BoolAnswer[No]
  final case class Number(recognizedText: RecognizedText, value: Long) extends Intent
  final case class UselessIntent(recognizedText: RecognizedText) extends Intent

  object Intent {

    def apply(recognizedText: RecognizedText) = recognizedText match {
      case "yes" | "positive" | "ya"   => Yes(recognizedText)
      case "no" | "nope" | "negative" => No(recognizedText)
      case _ => {
        val maybeNumber = recognizedText.toLongOption
        maybeNumber match {
          case Some(number) => Number(recognizedText, number)
          case None         => UselessIntent(recognizedText)
        }
      }
    }
  }
}
