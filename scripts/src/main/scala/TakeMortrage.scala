package Scripts

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import Domain.Entities._
import Domain.Replies._
import Domain.Intents._

object TakeMortrage {
  sealed trait Command

  final case class WhatToReply(intent: Intent, replyTo: ActorRef[Reply]) extends Command

  def apply(): Behavior[Command] = greet()

  private def greet(): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case WhatToReply(_, replyTo) => {
      replyTo ! WantToTakeMortrageReply()
      askedQuotation()
    }
    case _ => Behaviors.same
  }

  private def askedQuotation(): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case WhatToReply(intent, replyTo) =>
      intent match {
        case ba: BoolAnswer[_] =>
          ba match {
            case Yes(_) => {
              replyTo ! InitialSumQuotationReply()
              waitInitialSumAnswer()
            }
            case No(_) => {
              replyTo ! GoodbuyReply()
              greet()
            }
          }
        case _ => {
          replyTo ! SomethingGoesWrongReply()
          greet()
        }
      }
  }

  private def waitInitialSumAnswer(): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case WhatToReply(Number(_, initialSum), replyTo) => {
      replyTo ! TotalSumQuotationReply()
      waitTotalSumAnswer(initialSum = initialSum)
    }
    case WhatToReply(_, replyTo) => {
      replyTo ! SomethingGoesWrongReply()
      greet()
    }
  }

  private def waitTotalSumAnswer(initialSum: Long): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case WhatToReply(Number(_, totalSum), replyTo) => {
        if (initialSum.toFloat / totalSum.toFloat < 0.15) {
          replyTo ! ToSmallInitialSumReply(part = initialSum.toFloat / totalSum.toFloat)
        } else {
          replyTo ! OkToTakeMortrageReply()
        }
        greet()
      }
      case WhatToReply(_, replyTo) => {
        replyTo ! SomethingGoesWrongReply()
        greet()
      }
    }
}
