package Scripts

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.SnapshotSelectionCriteria
import akka.persistence.typed.scaladsl.Recovery

import Domain.Entities._
import Domain.Replies._
import Domain.Intents._
import Domain.JsonSerializable
import javax.smartcardio.CardTerminals.State

object PersistScript {

  sealed trait State extends JsonSerializable {
    def whatToReply: Reply
  }

  case object InitState extends State {
    override def whatToReply: Reply = WantToTakeMortrageReply()
  }

  case object InternalError extends State {
    override def whatToReply: Reply = SomethingGoesWrongReply()
  }

  case object AskedQuestion extends State {
    override def whatToReply: Reply = WantToTakeMortrageReply()
  }

  case object NoAnswerResived extends State {
    override def whatToReply: Reply = GoodbuyReply()
  }

  case object YesAnswerResived extends State {
    override def whatToReply: Reply = InitialSumQuotationReply()
  }

  // case object AskedInitialSumQuestion extends State {
  //   override def whatToReply: Reply = InitialSumQuotationReply()
  // }

  final case class AskedTotalSumQuestion(initialSum: Long) extends State {
    override def whatToReply: Reply = TotalSumQuotationReply()
  }

  final case class TotalSumAnswered(initialSum: Long, totalSum: Long) extends State {
    val part = initialSum.toFloat / totalSum.toFloat
    def isEnoughInitialSum: Boolean = part > 0.15

    override def whatToReply: Reply =
      if (isEnoughInitialSum) OkToTakeMortrageReply() else ToSmallInitialSumReply(part = part)
  }

  case object MortrageCongrats extends State {
    override def whatToReply: Reply = GoodbuyReply()
  }

  // команды
  sealed trait Command extends JsonSerializable

  final case class UserSaidCommand(
    intent: Intent,
    replyTo: ActorRef[StatusReply[UserSaidEventDone.type]]
  ) extends Command
  final case class WhatToReply(replyTo: ActorRef[Reply]) extends Command

  sealed trait Event extends JsonSerializable
  case class UserSaidEvent(intent: Intent) extends Event
  case object UserSaidEventDone
  case class Replied(reply: Reply) extends Event
  case class InitialSumResived(initialSum: Long) extends Event
  case object InitialSumSaved
  case class TotalSumResived(totalSum: Long) extends Event
  case object TotalSumSaved

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info("Starting PersistScript {}", entityId)
      EventSourcedBehavior[Command, Event, State](
        persistenceId,
        emptyState = InitState,
        commandHandler,
        eventHandler
      )
        .withRecovery(Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none))
    }
  }

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case cmd: UserSaidCommand => addUserSaid(cmd)
      case WhatToReply(replyTo) => Effect.reply(replyTo)(state.whatToReply)
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    state match {
      case InitState => AskedQuestion
      case AskedQuestion =>
        event match {
          case UserSaidEvent(intent) =>
            intent match {
              case No(_)  => NoAnswerResived
              case Yes(_) => YesAnswerResived
              case _      => AskedQuestion
            }
          case _ => AskedQuestion
        }
      case YesAnswerResived =>
        event match {
          case UserSaidEvent(intent) =>
            intent match {
              case Number(_, initialSum) =>
                addInitialSum(InitialSumResived(initialSum = initialSum))
                AskedTotalSumQuestion(initialSum = initialSum)
              case _ => InternalError
            }
          case _ => YesAnswerResived
        }
      case AskedTotalSumQuestion(initialSum) =>
        event match {
          case UserSaidEvent(intent) =>
            intent match {
              case Number(_, totalSum) =>
                addTotalSum(TotalSumResived(totalSum = totalSum))
                TotalSumAnswered(initialSum = initialSum, totalSum = totalSum)
              case _ => InternalError
            }
          case _ => AskedTotalSumQuestion(initialSum)
        }
      case TotalSumAnswered(initialSum, totalSum) =>
        event match {
          case _ => MortrageCongrats
        }
      case InternalError | MortrageCongrats | NoAnswerResived => InitState
    }
  }

  private def addUserSaid(cmd: UserSaidCommand): Effect[Event, State] = {
    Effect.persist(UserSaidEvent(intent = cmd.intent)).thenReply(cmd.replyTo) { _ =>
      StatusReply.Success(UserSaidEventDone)
    }
  }

  private def addInitialSum(event: InitialSumResived): Effect[Event, State] = {
    Effect.persist(event)
  }

  private def addTotalSum(event: TotalSumResived): Effect[Event, State] = {
    Effect.persist(event)
  }
}
