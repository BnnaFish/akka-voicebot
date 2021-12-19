package Domain

import Domain.Entities.TextToReply
import Domain.JsonSerializable

object Replies {

  sealed trait Reply extends JsonSerializable {
    val textToReply: TextToReply
  }
  final case class GreetReply(textToReply: TextToReply = "What question do you have?") extends Reply

  final case class WantToTakeMortgageReply(textToReply: TextToReply = "Do you what to buy a house?")
      extends Reply

  final case class GoodbuyReply(textToReply: TextToReply = "Any other questions?") extends Reply

  final case class InitialSumQuestionReply(
    textToReply: TextToReply = "What amount of initial sum do you allready have?"
  ) extends Reply

  final case class TotalSumQuestionReply(
    textToReply: TextToReply = "What amount of mortgage do you need?"
  ) extends Reply

  final case class ToSmallInitialSumReply(
    textToReply: TextToReply = "Sorry, but not enough initial sum.",
    part: Float
  ) extends Reply

  final case class OkToTakeMortgageReply(
    textToReply: TextToReply = "Mortgage is approved. Congratulations!"
  ) extends Reply

  final case class SomethingGoesWrongReply(
    textToReply: TextToReply = "Something goes wrong. Оperator will call you back soon."
  ) extends Reply
}
