package Domain

import Domain.Entities.TextToReply
import Domain.JsonSerializable

object Replies {

  sealed trait Reply extends JsonSerializable {
    val textToReply: TextToReply
  }
  final case class GreetReply(textToReply: TextToReply = "Я бот. Какой у вас вопрос?") extends Reply

  final case class WantToTakeMortrageReply(textToReply: TextToReply = "Вы хотите взять ипотеку?")
      extends Reply

  final case class GoodbuyReply(textToReply: TextToReply = "Есть ли еще вопросы?") extends Reply

  final case class InitialSumQuotationReply(
    textToReply: TextToReply = "Какой у вас первоначальный взнос?"
  ) extends Reply

  final case class TotalSumQuotationReply(
    textToReply: TextToReply = "На какую сумму ипотека?"
  ) extends Reply

  final case class ToSmallInitialSumReply(
    textToReply: TextToReply = "Слишком маленький первоначальный взнос",
    part: Float
  ) extends Reply

  final case class OkToTakeMortrageReply(
    textToReply: TextToReply = "Вам одобрена ипотека"
  ) extends Reply

  final case class SomethingGoesWrongReply(
    textToReply: TextToReply = "Произошла ошибка. Оператор вам перезвонит"
  ) extends Reply
}
